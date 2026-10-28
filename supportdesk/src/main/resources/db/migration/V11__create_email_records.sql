CREATE TABLE email_processing_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_id VARCHAR(255) UNIQUE,
    from_address VARCHAR(255) NOT NULL,
    subject VARCHAR(255),
    ticket_id BIGINT,
    status ENUM('PROCESSED','FAILED','DUPLICATE','IGNORED') NOT NULL,
    error_message TEXT,
    processed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ticket_id) REFERENCES tickets(id)
);
