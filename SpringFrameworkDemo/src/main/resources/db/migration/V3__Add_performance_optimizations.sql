-- Netflix Spring Framework Demo - Performance Optimizations
-- This migration adds performance optimizations and additional indexes

-- Add composite indexes for common query patterns
CREATE INDEX idx_users_status_created_at ON users(status, created_at);
CREATE INDEX idx_users_email_status ON users(email, status);
CREATE INDEX idx_users_age_status ON users(age, status);

-- Add indexes for user_profiles common queries
CREATE INDEX idx_user_profiles_status_location ON user_profiles(profile_status, location);
CREATE INDEX idx_user_profiles_company_job_title ON user_profiles(company, job_title);
CREATE INDEX idx_user_profiles_experience_years ON user_profiles(experience_years);

-- Add indexes for roles common queries
CREATE INDEX idx_roles_type_active ON roles(role_type, is_active);
CREATE INDEX idx_roles_priority_active ON roles(priority, is_active);

-- Add indexes for user_roles common queries
CREATE INDEX idx_user_roles_created_at ON user_roles(created_at);

-- Add partial indexes for soft delete queries
CREATE INDEX idx_users_active ON users(id) WHERE deleted_at IS NULL;
CREATE INDEX idx_users_deleted ON users(id) WHERE deleted_at IS NOT NULL;

-- Add function-based indexes for case-insensitive searches
CREATE INDEX idx_users_name_lower ON users(LOWER(name));
CREATE INDEX idx_users_email_lower ON users(LOWER(email));

-- Add indexes for user_profiles case-insensitive searches
CREATE INDEX idx_user_profiles_location_lower ON user_profiles(LOWER(location));
CREATE INDEX idx_user_profiles_job_title_lower ON user_profiles(LOWER(job_title));
CREATE INDEX idx_user_profiles_company_lower ON user_profiles(LOWER(company));

-- Add indexes for roles case-insensitive searches
CREATE INDEX idx_roles_name_lower ON roles(LOWER(name));
CREATE INDEX idx_roles_code_lower ON roles(LOWER(code));

-- Create materialized view for user statistics (if supported)
-- Note: This is a placeholder as H2 doesn't support materialized views
-- In production with PostgreSQL, you would create materialized views here

-- Add constraints for data integrity
ALTER TABLE users ADD CONSTRAINT chk_users_age CHECK (age IS NULL OR age BETWEEN 0 AND 150);
ALTER TABLE users ADD CONSTRAINT chk_users_email_format CHECK (email LIKE '%@%');
ALTER TABLE users ADD CONSTRAINT chk_users_phone_format CHECK (phone_number IS NULL OR phone_number REGEXP '^\\+?[1-9]\\d{1,14}$');

ALTER TABLE user_profiles ADD CONSTRAINT chk_user_profiles_experience_years CHECK (experience_years IS NULL OR experience_years BETWEEN 0 AND 100);
ALTER TABLE user_profiles ADD CONSTRAINT chk_user_profiles_website_format CHECK (website IS NULL OR website LIKE 'http%');

ALTER TABLE roles ADD CONSTRAINT chk_roles_priority CHECK (priority >= 0);
ALTER TABLE roles ADD CONSTRAINT chk_roles_code_format CHECK (code REGEXP '^[A-Z_]+$');
