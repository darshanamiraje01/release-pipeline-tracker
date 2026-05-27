-- Sample Releases
INSERT INTO releases (name, version, description, stage, created_by) VALUES
('Spring Boot Migration', 'v1.0.0', 'Migrate legacy services to Spring Boot 3.x', 'PLANNING', 'admin'),
('Payment Gateway Integration', 'v2.1.0', 'Integrate Razorpay payment gateway', 'IN_PROGRESS', 'admin'),
('Security Patch Release', 'v1.5.2', 'Critical JWT vulnerability fix', 'COMPLETED', 'admin');

-- Sample Tasks for Release 1 (PLANNING)
INSERT INTO tasks (title, description, assignee, status, priority, release_id) VALUES
('Define scope document', 'List all services to be migrated', 'darshana', 'PENDING', 'HIGH', 1),
('Set up new project structure', 'Initialize Spring Boot 3.x parent POM', 'rahul', 'PENDING', 'HIGH', 1),
('Identify deprecated APIs', 'Audit existing codebase for deprecated Spring 2.x APIs', 'priya', 'PENDING', 'MEDIUM', 1);

-- Sample Tasks for Release 2 (IN_PROGRESS)
INSERT INTO tasks (title, description, assignee, status, priority, release_id) VALUES
('Create Razorpay account', 'Set up merchant account and get API keys', 'admin', 'DONE', 'HIGH', 2),
('Implement payment service', 'Build PaymentService with Razorpay SDK', 'darshana', 'IN_PROGRESS', 'HIGH', 2),
('Write integration tests', 'Cover payment success, failure, and refund flows', 'rahul', 'PENDING', 'MEDIUM', 2),
('Update API documentation', 'Add payment endpoints to Swagger UI', 'priya', 'PENDING', 'LOW', 2);

-- Sample Tasks for Release 3 (COMPLETED)
INSERT INTO tasks (title, description, assignee, status, priority, release_id) VALUES
('Patch JWT secret rotation', 'Implement secret rotation without downtime', 'darshana', 'DONE', 'HIGH', 3),
('Update security config', 'Tighten CORS and CSRF policies', 'rahul', 'DONE', 'HIGH', 3),
('Deploy to production', 'Blue-green deployment with rollback plan', 'admin', 'DONE', 'HIGH', 3);
