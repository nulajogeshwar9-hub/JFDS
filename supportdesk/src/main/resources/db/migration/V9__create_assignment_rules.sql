CREATE TABLE assignment_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    organization_id BIGINT NOT NULL,
    domain VARCHAR(50),
    category_id BIGINT,
    priority ENUM('URGENT','HIGH','MEDIUM','LOW'),
    assign_team_id BIGINT,
    assign_agent_id BIGINT,
    rule_order INT DEFAULT 0,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (organization_id) REFERENCES organizations(id),
    FOREIGN KEY (category_id) REFERENCES ticket_categories(id),
    FOREIGN KEY (assign_team_id) REFERENCES support_teams(id),
    FOREIGN KEY (assign_agent_id) REFERENCES users(id)
);
