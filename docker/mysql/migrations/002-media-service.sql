CREATE DATABASE IF NOT EXISTS multishop_media;

CREATE TABLE IF NOT EXISTS multishop_media.images (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    name VARCHAR(255),
    image_url VARCHAR(255),
    image_id VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL
);

INSERT IGNORE INTO multishop_media.images (id, name, image_url, image_id, status, created_at)
SELECT id, name, image_url, image_id, 'CONFIRMED', NOW(6)
FROM images;

SET @fk = (SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'images_to_products' AND REFERENCED_TABLE_NAME = 'images' LIMIT 1);
SET @sql = IF(@fk IS NULL, 'DO 0', CONCAT('ALTER TABLE images_to_products DROP FOREIGN KEY ', @fk));
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;

SET @fk = (SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND REFERENCED_TABLE_NAME = 'images' LIMIT 1);
SET @sql = IF(@fk IS NULL, 'DO 0', CONCAT('ALTER TABLE users DROP FOREIGN KEY ', @fk));
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;
