terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.0"
    }
    helm = {
      source  = "hashicorp/helm"
      version = "~> 2.0"
    }
  }

  backend "s3" {
    bucket         = "payments-platform-terraform-state"
    key            = "payments-platform/terraform.tfstate"
    region         = "us-east-1"
    encrypt        = true
    dynamodb_table = "payments-platform-terraform-locks"
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "payments-platform"
      Environment = var.environment
      ManagedBy   = "terraform"
      Owner       = "platform-team"
    }
  }
}

# VPC Configuration
module "vpc" {
  source = "./modules/vpc"

  name = "payments-platform-${var.environment}"
  cidr = var.vpc_cidr

  azs             = var.availability_zones
  private_subnets = var.private_subnets
  public_subnets  = var.public_subnets

  enable_nat_gateway     = true
  single_nat_gateway     = var.environment == "dev"
  enable_dns_hostnames   = true
  enable_dns_support     = true

  tags = {
    Name = "payments-platform-${var.environment}"
  }
}

# EKS Cluster
module "eks" {
  source  = "terraform-aws-modules/eks/aws"
  version = "~> 19.0"

  cluster_name    = "payments-platform-${var.environment}"
  cluster_version = var.eks_version

  vpc_id     = module.vpc.vpc_id
  subnet_ids = module.vpc.private_subnets

  # EKS Managed Node Groups
  eks_managed_node_groups = {
    core = {
      name            = "core-node-group"
      instance_types  = ["t3.medium"]
      min_size        = 2
      max_size        = 10
      desired_size    = 3
      capacity_type   = "ON_DEMAND"

      labels = {
        Environment = var.environment
        NodeGroup   = "core"
      }

      taints = []
    }

    application = {
      name            = "app-node-group"
      instance_types  = ["t3.large"]
      min_size        = 3
      max_size        = 20
      desired_size    = 5
      capacity_type   = "ON_DEMAND"

      labels = {
        Environment = var.environment
        NodeGroup   = "application"
      }

      taints = []
    }

    spot = {
      name            = "spot-node-group"
      instance_types  = ["t3.medium", "t3.large"]
      min_size        = 0
      max_size        = 50
      desired_size    = 0
      capacity_type   = "SPOT"

      labels = {
        Environment = var.environment
        NodeGroup   = "spot"
      }
    }
  }

  # Security Groups
  node_security_group_additional_rules = {
    ingress_self_all = {
      description = "Node to node all ports/protocols"
      protocol    = "-1"
      from_port   = 0
      to_port     = 0
      type        = "ingress"
      self        = true
    }
  }

  # Addons
  cluster_addons = {
    coredns = {
      most_recent = true
    }
    kube-proxy = {
      most_recent = true
    }
    vpc-cni = {
      most_recent = true
    }
    aws-ebs-csi-driver = {
      most_recent = true
    }
  }

  tags = {
    Environment = var.environment
  }
}

# RDS PostgreSQL
module "rds" {
  source  = "terraform-aws-modules/rds/aws"
  version = "~> 6.0"

  identifier = "payments-platform-${var.environment}"

  engine               = "postgres"
  engine_version       = "15.4"
  family               = "postgres15"
  major_engine_version = "15"
  instance_class       = var.rds_instance_class

  allocated_storage     = var.rds_allocated_storage
  max_allocated_storage = var.rds_max_allocated_storage

  db_name  = "payments_platform"
  username = "payments_user"
  port     = 5432

  multi_az               = var.environment == "prod"
  db_subnet_group_name   = module.vpc.database_subnet_group_name
  vpc_security_group_ids = [module.rds_security_group.security_group_id]

  maintenance_window              = "Mon:00:00-Mon:03:00"
  backup_window                   = "03:00-06:00"
  enabled_cloudwatch_logs_exports = ["postgresql"]
  create_cloudwatch_log_group     = true

  backup_retention_period = var.environment == "prod" ? 35 : 7
  skip_final_snapshot     = var.environment != "prod"
  deletion_protection     = var.environment == "prod"

  performance_insights_enabled          = true
  performance_insights_retention_period = 7
  create_monitoring_role                = true

  tags = {
    Environment = var.environment
  }
}

# ElastiCache Redis
module "redis" {
  source  = "terraform-aws-modules/elasticache/aws"
  version = "~> 1.0"

  cluster_id               = "payments-platform-${var.environment}"
  create_cluster           = true
  engine                   = "redis"
  node_type                = var.redis_node_type
  num_cache_nodes          = var.environment == "prod" ? 2 : 1
  parameter_group_name     = "default.redis7"
  port                     = 6379
  maintenance_window       = "tue:06:30-tue:07:30"
  snapshot_window          = "05:00-06:00"
  snapshot_retention_limit = var.environment == "prod" ? 7 : 1

  subnet_ids          = module.vpc.database_subnets
  security_group_ids  = [module.redis_security_group.security_group_id]

  apply_immediately = var.environment != "prod"

  tags = {
    Environment = var.environment
  }
}

# S3 Buckets
module "s3_backup" {
  source  = "terraform-aws-modules/s3-bucket/aws"
  version = "~> 3.0"

  bucket = "payments-platform-backups-${var.environment}-${random_string.suffix.result}"

  versioning = {
    enabled = true
  }

  server_side_encryption_configuration = {
    rule = {
      apply_server_side_encryption_by_default = {
        sse_algorithm = "AES256"
      }
      bucket_key_enabled = true
    }
  }

  lifecycle_rule = [
    {
      id      = "backup_lifecycle"
      enabled = true

      transition = [
        {
          days          = 30
          storage_class = "STANDARD_IA"
        },
        {
          days          = 90
          storage_class = "GLACIER"
        },
        {
          days          = 365
          storage_class = "DEEP_ARCHIVE"
        }
      ]

      expiration = {
        days = 2555  # 7 years
      }
    }
  ]

  tags = {
    Environment = var.environment
    Purpose     = "backup"
  }
}

# CloudFront Distribution
module "cloudfront" {
  source  = "terraform-aws-modules/cloudfront/aws"
  version = "~> 3.0"

  aliases = [var.domain_name]

  comment             = "Payments Platform CDN"
  enabled             = true
  is_ipv6_enabled     = true
  price_class         = "PriceClass_100"
  retain_on_delete    = false
  wait_for_deployment = false

  # Origin configuration for API Gateway
  origin = {
    api_gateway = {
      domain_name = module.api_gateway.api_endpoint
      custom_origin_config = {
        http_port              = 80
        https_port             = 443
        origin_protocol_policy = "https-only"
        origin_ssl_protocols   = ["TLSv1.2"]
      }
    }
  }

  # Default cache behavior
  default_cache_behavior = {
    target_origin_id       = "api_gateway"
    viewer_protocol_policy = "redirect-to-https"

    allowed_methods = ["GET", "HEAD", "OPTIONS", "PUT", "POST", "PATCH", "DELETE"]
    cached_methods  = ["GET", "HEAD"]
    compress        = true

    forwarded_values = {
      query_string = true
      cookies = {
        forward = "all"
      }
    }

    min_ttl     = 0
    default_ttl = 86400
    max_ttl     = 31536000
  }

  # SSL/TLS configuration
  viewer_certificate = {
    acm_certificate_arn      = module.acm.acm_certificate_arn
    ssl_support_method       = "sni-only"
    minimum_protocol_version = "TLSv1.2_2021"
  }

  tags = {
    Environment = var.environment
  }
}

# Route 53 Records
module "route53" {
  source  = "terraform-aws-modules/route53/aws"
  version = "~> 2.0"

  records = [
    {
      name    = var.domain_name
      type    = "A"
      alias = {
        name                   = module.cloudfront.cloudfront_distribution_domain_name
        zone_id               = module.cloudfront.cloudfront_distribution_zone_id
        evaluate_target_health = true
      }
    },
    {
      name    = "api.${var.domain_name}"
      type    = "A"
      alias = {
        name                   = module.api_gateway.api_endpoint
        zone_id               = module.api_gateway.api_zone_id
        evaluate_target_health = true
      }
    }
  ]

  tags = {
    Environment = var.environment
  }
}

# ACM Certificate
module "acm" {
  source  = "terraform-aws-modules/acm/aws"
  version = "~> 4.0"

  domain_name               = var.domain_name
  zone_id                   = data.aws_route53_zone.this.id
  subject_alternative_names = ["*.${var.domain_name}"]

  wait_for_validation = true

  tags = {
    Environment = var.environment
  }
}

# Security Groups
module "rds_security_group" {
  source  = "terraform-aws-modules/security-group/aws"
  version = "~> 5.0"

  name        = "rds-sg"
  description = "Security group for RDS"
  vpc_id      = module.vpc.vpc_id

  ingress_with_cidr_blocks = [
    {
      from_port   = 5432
      to_port     = 5432
      protocol    = "tcp"
      description = "PostgreSQL access from VPC"
      cidr_blocks = module.vpc.vpc_cidr_block
    }
  ]

  tags = {
    Environment = var.environment
  }
}

module "redis_security_group" {
  source  = "terraform-aws-modules/security-group/aws"
  version = "~> 5.0"

  name        = "redis-sg"
  description = "Security group for Redis"
  vpc_id      = module.vpc.vpc_id

  ingress_with_cidr_blocks = [
    {
      from_port   = 6379
      to_port     = 6379
      protocol    = "tcp"
      description = "Redis access from VPC"
      cidr_blocks = module.vpc.vpc_cidr_block
    }
  ]

  tags = {
    Environment = var.environment
  }
}

# Random suffix for globally unique resources
resource "random_string" "suffix" {
  length  = 8
  lower   = true
  upper   = false
  numeric = true
  special = false
}

# Data sources
data "aws_route53_zone" "this" {
  name         = var.domain_name
  private_zone = false
}

# Outputs
output "cluster_name" {
  description = "EKS cluster name"
  value       = module.eks.cluster_name
}

output "cluster_endpoint" {
  description = "EKS cluster endpoint"
  value       = module.eks.cluster_endpoint
}

output "database_endpoint" {
  description = "RDS database endpoint"
  value       = module.rds.db_instance_endpoint
}

output "redis_endpoint" {
  description = "Redis cluster endpoint"
  value       = module.redis.elasticache_replication_group_primary_endpoint_address
}

output "cloudfront_distribution_id" {
  description = "CloudFront distribution ID"
  value       = module.cloudfront.cloudfront_distribution_id
}

output "domain_name" {
  description = "Domain name for the application"
  value       = var.domain_name
}
