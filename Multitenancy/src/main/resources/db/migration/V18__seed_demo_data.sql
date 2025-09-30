-- Demo seed data for development and testing
-- This migration adds sample tenants, projects, and issues for demonstration

-- Insert demo tenants
INSERT INTO tenants (id, name, created_at, updated_at) VALUES 
('acme', 'ACME Corporation', NOW(), NOW()),
('netflix', 'Netflix Inc', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Insert demo projects
INSERT INTO projects (id, tenant_id, key, name, description, type, created_at, updated_at) VALUES 
('proj-1', 'acme', 'ACME', 'ACME Main Project', 'Main development project for ACME', 'SOFTWARE', NOW(), NOW()),
('proj-2', 'acme', 'ACME-API', 'ACME API Project', 'API development project', 'SOFTWARE', NOW(), NOW()),
('proj-3', 'netflix', 'NETFLIX', 'Netflix Platform', 'Netflix streaming platform', 'SOFTWARE', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Insert demo issues
INSERT INTO issues (id, tenant_id, key, title, description, status, priority, type, project_id, assignee_id, reporter_id, created_at, updated_at) VALUES 
('issue-1', 'acme', 'ACME-1', 'Fix login bug', 'User cannot login with special characters in password', 'OPEN', 'HIGH', 'BUG', 'proj-1', 'user1', 'user1', NOW(), NOW()),
('issue-2', 'acme', 'ACME-2', 'Add user profile page', 'Create a new user profile management page', 'IN_PROGRESS', 'MEDIUM', 'STORY', 'proj-1', 'user2', 'user1', NOW(), NOW()),
('issue-3', 'acme', 'ACME-3', 'API rate limiting', 'Implement rate limiting for API endpoints', 'OPEN', 'HIGH', 'TASK', 'proj-2', 'user3', 'user2', NOW(), NOW()),
('issue-4', 'netflix', 'NETFLIX-1', 'Streaming optimization', 'Optimize video streaming performance', 'RESOLVED', 'CRITICAL', 'IMPROVEMENT', 'proj-3', 'user4', 'user4', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Insert demo users (for reference)
INSERT INTO users (id, tenant_id, username, email, first_name, last_name, created_at, updated_at) VALUES 
('user1', 'acme', 'john.doe', 'john.doe@acme.com', 'John', 'Doe', NOW(), NOW()),
('user2', 'acme', 'jane.smith', 'jane.smith@acme.com', 'Jane', 'Smith', NOW(), NOW()),
('user3', 'acme', 'bob.wilson', 'bob.wilson@acme.com', 'Bob', 'Wilson', NOW(), NOW()),
('user4', 'netflix', 'alice.johnson', 'alice.johnson@netflix.com', 'Alice', 'Johnson', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;
