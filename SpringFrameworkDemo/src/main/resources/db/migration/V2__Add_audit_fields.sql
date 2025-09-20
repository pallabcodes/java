-- Netflix Spring Framework Demo - Add Audit Fields
-- This migration adds comprehensive audit fields to all tables

-- Add audit fields to users table
ALTER TABLE users ADD COLUMN created_by VARCHAR(100) NULL;
ALTER TABLE users ADD COLUMN updated_by VARCHAR(100) NULL;
ALTER TABLE users ADD COLUMN last_login_at TIMESTAMP NULL;
ALTER TABLE users ADD COLUMN login_count INTEGER DEFAULT 0;

-- Add audit fields to user_profiles table
ALTER TABLE user_profiles ADD COLUMN created_by VARCHAR(100) NULL;
ALTER TABLE user_profiles ADD COLUMN updated_by VARCHAR(100) NULL;

-- Add audit fields to roles table
ALTER TABLE roles ADD COLUMN created_by VARCHAR(100) NULL;
ALTER TABLE roles ADD COLUMN updated_by VARCHAR(100) NULL;

-- Add audit fields to user_roles table
ALTER TABLE user_roles ADD COLUMN created_by VARCHAR(100) NULL;

-- Create indexes for audit fields
CREATE INDEX idx_users_created_by ON users(created_by);
CREATE INDEX idx_users_updated_by ON users(updated_by);
CREATE INDEX idx_users_last_login_at ON users(last_login_at);

-- Update existing records with default audit values
UPDATE users SET created_by = 'SYSTEM', updated_by = 'SYSTEM' WHERE created_by IS NULL;
UPDATE user_profiles SET created_by = 'SYSTEM', updated_by = 'SYSTEM' WHERE created_by IS NULL;
UPDATE roles SET created_by = 'SYSTEM', updated_by = 'SYSTEM' WHERE created_by IS NULL;
UPDATE user_roles SET created_by = 'SYSTEM' WHERE created_by IS NULL;
