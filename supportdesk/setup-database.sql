-- ==============================================================
-- SupportDesk - MySQL Database Initialization Script
-- Run this in MySQL CLI or MySQL Workbench if not already created
-- ==============================================================

CREATE DATABASE IF NOT EXISTS supportdesk
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Optional: Create a dedicated application user
-- CREATE USER IF NOT EXISTS 'supportdesk_user'@'localhost' IDENTIFIED BY 'SupportDesk@2026';
-- GRANT ALL PRIVILEGES ON supportdesk.* TO 'supportdesk_user'@'localhost';
-- FLUSH PRIVILEGES;

USE supportdesk;

-- Note: All table schemas, foreign keys, constraints, and seed data
-- are automatically created and managed by Flyway migrations in:
-- classpath:db/migration (V1__ to V12__)

