CREATE DATABASE IF NOT EXISTS multishop_media;

CREATE USER IF NOT EXISTS 'media'@'%' IDENTIFIED BY 'media';
GRANT ALL PRIVILEGES ON multishop_media.* TO 'media'@'%';

CREATE TABLE IF NOT EXISTS multishop_media.images (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    name VARCHAR(255),
    image_url VARCHAR(255),
    image_id VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL
);
