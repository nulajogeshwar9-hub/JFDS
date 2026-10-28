-- Seed Organization
INSERT INTO organizations (id, name, domain, description, business_hours_start, business_hours_end, timezone, active)
VALUES (1, 'TechFlow Solutions', 'IT_SOFTWARE', 'Leading IT software solutions provider', '09:00:00', '17:00:00', 'UTC', TRUE);

-- Seed SLA Policies
INSERT INTO sla_policies (name, organization_id, priority, first_response_minutes, resolution_minutes, active) VALUES
('Urgent SLA', 1, 'URGENT', 30, 240, TRUE),
('High SLA', 1, 'HIGH', 60, 480, TRUE),
('Medium SLA', 1, 'MEDIUM', 240, 1440, TRUE),
('Low SLA', 1, 'LOW', 480, 2880, TRUE);

-- Seed Support Teams
INSERT INTO support_teams (id, name, description, organization_id, active) VALUES
(1, 'Technical Support', 'Handles technical issues and bug reports', 1, TRUE),
(2, 'Billing Support', 'Handles payment and billing inquiries', 1, TRUE),
(3, 'General Support', 'Handles general inquiries and feature requests', 1, TRUE);

-- Seed Ticket Categories
INSERT INTO ticket_categories (id, name, description, organization_id, parent_id, active) VALUES
(1, 'Login Issues', 'Problems with authentication and account access', 1, NULL, TRUE),
(2, 'Payment Issues', 'Billing, invoicing, and payment problems', 1, NULL, TRUE),
(3, 'Feature Request', 'Requests for new features or enhancements', 1, NULL, TRUE),
(4, 'Bug Report', 'Software bugs and defects', 1, NULL, TRUE),
(5, 'General Inquiry', 'General questions and information requests', 1, NULL, TRUE);

-- Seed Assignment Rules
INSERT INTO assignment_rules (name, organization_id, domain, category_id, priority, assign_team_id, assign_agent_id, rule_order, active) VALUES
('Login to Technical', 1, NULL, 1, NULL, 1, NULL, 1, TRUE),
('Payment to Billing', 1, NULL, 2, NULL, 2, NULL, 2, TRUE),
('Bug to Technical', 1, NULL, 4, NULL, 1, NULL, 3, TRUE);
