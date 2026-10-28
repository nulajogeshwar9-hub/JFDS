CREATE TABLE sla_policies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    organization_id BIGINT NOT NULL,
    priority ENUM('URGENT','HIGH','MEDIUM','LOW') NOT NULL,
    first_response_minutes INT NOT NULL,
    resolution_minutes INT NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (organization_id) REFERENCES organizations(id)
);
