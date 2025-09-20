-- Netflix Spring Framework Demo - Initial Database Schema
-- This migration creates the initial database schema with all tables and constraints

-- Create users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    age INTEGER,
    phone_number VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    deleted_at TIMESTAMP NULL
);

-- Create indexes for users table
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_name ON users(name);
CREATE INDEX idx_user_created_at ON users(created_at);
CREATE INDEX idx_user_deleted_at ON users(deleted_at);

-- Create user_profiles table
CREATE TABLE user_profiles (
    user_id BIGINT PRIMARY KEY,
    bio VARCHAR(1000),
    location VARCHAR(500),
    website VARCHAR(100),
    phone_number VARCHAR(20),
    experience_years INTEGER,
    job_title VARCHAR(100),
    company VARCHAR(200),
    profile_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for user_profiles table
CREATE INDEX idx_user_profile_bio ON user_profiles(bio);
CREATE INDEX idx_user_profile_created_at ON user_profiles(created_at);

-- Create roles table
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(500),
    role_type VARCHAR(20) NOT NULL DEFAULT 'USER',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    priority INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

-- Create indexes for roles table
CREATE INDEX idx_role_name ON roles(name);
CREATE INDEX idx_role_code ON roles(code);
CREATE INDEX idx_role_created_at ON roles(created_at);

-- Create user_roles junction table
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Create indexes for user_roles table
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);

-- Insert default roles
INSERT INTO roles (name, code, description, role_type, priority) VALUES
('Administrator', 'ADMIN', 'System administrator with full access', 'SYSTEM', 100),
('User', 'USER', 'Regular user with basic access', 'USER', 10),
('Moderator', 'MODERATOR', 'User with moderation privileges', 'CUSTOM', 50),
('Guest', 'GUEST', 'Guest user with limited access', 'CUSTOM', 5);

-- Insert sample users
INSERT INTO users (name, email, age, phone_number, status) VALUES
('John Doe', 'john.doe@netflix.com', 30, '+1-555-0123', 'ACTIVE'),
('Jane Smith', 'jane.smith@netflix.com', 25, '+1-555-0124', 'ACTIVE'),
('Bob Johnson', 'bob.johnson@netflix.com', 35, '+1-555-0125', 'ACTIVE'),
('Alice Brown', 'alice.brown@netflix.com', 28, '+1-555-0126', 'INACTIVE');

-- Insert sample user profiles
INSERT INTO user_profiles (user_id, bio, location, website, job_title, company, experience_years) VALUES
(1, 'Senior Software Engineer with expertise in Java and Spring Framework', 'San Francisco, CA', 'https://johndoe.dev', 'Senior Software Engineer', 'Netflix', 8),
(2, 'Full-stack developer passionate about building scalable applications', 'New York, NY', 'https://janesmith.dev', 'Full-stack Developer', 'Netflix', 5),
(3, 'DevOps Engineer focused on cloud infrastructure and automation', 'Seattle, WA', 'https://bobjohnson.dev', 'DevOps Engineer', 'Netflix', 10),
(4, 'Product Manager with experience in streaming platforms', 'Los Angeles, CA', 'https://alicebrown.dev', 'Product Manager', 'Netflix', 6);

-- Assign roles to users
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1), -- John Doe is Admin
(2, 2), -- Jane Smith is User
(3, 3), -- Bob Johnson is Moderator
(4, 2); -- Alice Brown is User
