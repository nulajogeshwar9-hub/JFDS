CREATE TABLE sla_breaches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    sla_policy_id BIGINT NOT NULL,
    breach_type ENUM('FIRST_RESPONSE','RESOLUTION') NOT NULL,
    deadline TIMESTAMP NOT NULL,
    breached_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ticket_id) REFERENCES tickets(id),
    FOREIGN KEY (sla_policy_id) REFERENCES sla_policies(id)
);
