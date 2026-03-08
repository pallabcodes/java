package com.yourorg.platform.clean.framework.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private Aws aws = new Aws();
    private Messaging messaging = new Messaging();
    private Outbox outbox = new Outbox();
    private Cache cache = new Cache();
    private Storage storage = new Storage();

    public Aws getAws() {
        return aws;
    }

    public void setAws(Aws aws) {
        this.aws = aws;
    }

    public Messaging getMessaging() {
        return messaging;
    }

    public void setMessaging(Messaging messaging) {
        this.messaging = messaging;
    }

    public Outbox getOutbox() {
        return outbox;
    }

    public void setOutbox(Outbox outbox) {
        this.outbox = outbox;
    }

    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public Storage getStorage() {
        return storage;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public static class Aws {
        private String region = "us-east-1";
        private String endpoint;
        private Sqs sqs = new Sqs();
        private Sns sns = new Sns();
        private DynamoDb dynamodb = new DynamoDb();
        private S3 s3 = new S3();

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public Sqs getSqs() {
            return sqs;
        }

        public void setSqs(Sqs sqs) {
            this.sqs = sqs;
        }

        public Sns getSns() {
            return sns;
        }

        public void setSns(Sns sns) {
            this.sns = sns;
        }

        public DynamoDb getDynamodb() {
            return dynamodb;
        }

        public void setDynamodb(DynamoDb dynamodb) {
            this.dynamodb = dynamodb;
        }

        public S3 getS3() {
            return s3;
        }

        public void setS3(S3 s3) {
            this.s3 = s3;
        }

        public static class Sqs {
            private String queueUrl;

            public String getQueueUrl() {
                return queueUrl;
            }

            public void setQueueUrl(String queueUrl) {
                this.queueUrl = queueUrl;
            }
        }

        public static class Sns {
            private String topicArn;

            public String getTopicArn() {
                return topicArn;
            }

            public void setTopicArn(String topicArn) {
                this.topicArn = topicArn;
            }
        }

        public static class DynamoDb {
            private String tableName;

            public String getTableName() {
                return tableName;
            }

            public void setTableName(String tableName) {
                this.tableName = tableName;
            }
        }

        public static class S3 {
            private String bucket;

            public String getBucket() {
                return bucket;
            }

            public void setBucket(String bucket) {
                this.bucket = bucket;
            }
        }
    }

    public static class Messaging {
        private String mode = "kafka";
        private Kafka kafka = new Kafka();
        private Rabbit rabbit = new Rabbit();

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public Kafka getKafka() {
            return kafka;
        }

        public void setKafka(Kafka kafka) {
            this.kafka = kafka;
        }

        public Rabbit getRabbit() {
            return rabbit;
        }

        public void setRabbit(Rabbit rabbit) {
            this.rabbit = rabbit;
        }

        public static class Kafka {
            private String topic;

            public String getTopic() {
                return topic;
            }

            public void setTopic(String topic) {
                this.topic = topic;
            }
        }

        public static class Rabbit {
            private String exchange;
            private String routingKey;

            public String getExchange() {
                return exchange;
            }

            public void setExchange(String exchange) {
                this.exchange = exchange;
            }

            public String getRoutingKey() {
                return routingKey;
            }

            public void setRoutingKey(String routingKey) {
                this.routingKey = routingKey;
            }
        }
    }

    public static class Outbox {
        private boolean pollingEnabled = false;
        private int batchSize = 100;
        private long pollIntervalMs = 5000;

        public boolean isPollingEnabled() {
            return pollingEnabled;
        }

        public void setPollingEnabled(boolean pollingEnabled) {
            this.pollingEnabled = pollingEnabled;
        }

        public int getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }

        public long getPollIntervalMs() {
            return pollIntervalMs;
        }

        public void setPollIntervalMs(long pollIntervalMs) {
            this.pollIntervalMs = pollIntervalMs;
        }
    }

    public static class Cache {
        private boolean enabled = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class Storage {
        private String mode = "s3";

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }
    }
}
