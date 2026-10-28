CREATE TABLE tickets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_number VARCHAR(20) NOT NULL UNIQUE,
    organization_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    subject VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    domain VARCHAR(50),
    category_id BIGINT,
    priority ENUM('URGENT','HIGH','MEDIUM','LOW') DEFAULT 'MEDIUM',
    status ENUM('OPEN','IN_PROGRESS','WAITING_FOR_CUSTOMER','RESOLVED','CLOSED') DEFAULT 'OPEN',
    assigned_team_id BIGINT,
    assigned_agent_id BIGINT,
    sla_policy_id BIGINT,
    first_response_deadline TIMESTAMP NULL,
    resolution_deadline TIMESTAMP NULL,
    first_response_at TIMESTAMP NULL,
    resolved_at TIMESTAMP NULL,
    closed_at TIMESTAMP NULL,
    sla_first_response_breached BOOLEAN DEFAULT FALSE,
    sla_resolution_breached BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (organization_id) REFERENCES organizations(id),
    FOREIGN KEY (customer_id) REFERENCES users(id),
    FOREIGN KEY (category_id) REFERENCES ticket_categories(id),
    FOREIGN KEY (assigned_team_id) REFERENCES support_teams(id),
    FOREIGN KEY (assigned_agent_id) REFERENCES users(id),
    FOREIGN KEY (sla_policy_id) REFERENCES sla_policies(id)
);
CREATE INDEX idx_tickets_org ON tickets(organization_id);
CREATE INDEX idx_tickets_customer ON tickets(customer_id);
CREATE INDEX idx_tickets_status ON tickets(status);
CREATE INDEX idx_tickets_agent ON tickets(assigned_agent_id);
CREATE INDEX idx_tickets_number ON tickets(ticket_number);
