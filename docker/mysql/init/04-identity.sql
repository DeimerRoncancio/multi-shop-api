CREATE DATABASE IF NOT EXISTS multishop_identity;

CREATE USER IF NOT EXISTS 'identity'@'%' IDENTIFIED BY 'identity';
GRANT ALL PRIVILEGES ON multishop_identity.* TO 'identity'@'%';

CREATE TABLE IF NOT EXISTS multishop_identity.roles (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    role VARCHAR(255) UNIQUE
);

INSERT IGNORE INTO multishop_identity.roles (id, role) VALUES
    (UUID(), 'ROLE_USER'),
    (UUID(), 'ROLE_ADMIN');
