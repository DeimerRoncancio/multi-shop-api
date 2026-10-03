CREATE DATABASE IF NOT EXISTS multishop_catalog;

CREATE TABLE IF NOT EXISTS multishop_catalog.products LIKE products;
CREATE TABLE IF NOT EXISTS multishop_catalog.prod_categories LIKE prod_categories;
CREATE TABLE IF NOT EXISTS multishop_catalog.variants LIKE variants;
CREATE TABLE IF NOT EXISTS multishop_catalog.categories_to_products LIKE categories_to_products;
CREATE TABLE IF NOT EXISTS multishop_catalog.images_to_products LIKE images_to_products;

INSERT IGNORE INTO multishop_catalog.products SELECT * FROM products;
INSERT IGNORE INTO multishop_catalog.prod_categories SELECT * FROM prod_categories;
INSERT IGNORE INTO multishop_catalog.variants SELECT * FROM variants;
INSERT IGNORE INTO multishop_catalog.categories_to_products SELECT * FROM categories_to_products;
INSERT IGNORE INTO multishop_catalog.images_to_products SELECT * FROM images_to_products;

SET @sql = IF(EXISTS(SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
                     WHERE CONSTRAINT_SCHEMA = 'multishop_catalog' AND CONSTRAINT_NAME = 'fk_categories_to_products_product'),
              'DO 0',
              'ALTER TABLE multishop_catalog.categories_to_products ADD CONSTRAINT fk_categories_to_products_product FOREIGN KEY (id_product) REFERENCES multishop_catalog.products (id)');
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;

SET @sql = IF(EXISTS(SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
                     WHERE CONSTRAINT_SCHEMA = 'multishop_catalog' AND CONSTRAINT_NAME = 'fk_categories_to_products_category'),
              'DO 0',
              'ALTER TABLE multishop_catalog.categories_to_products ADD CONSTRAINT fk_categories_to_products_category FOREIGN KEY (id_category) REFERENCES multishop_catalog.prod_categories (id)');
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;

SET @sql = IF(EXISTS(SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
                     WHERE CONSTRAINT_SCHEMA = 'multishop_catalog' AND CONSTRAINT_NAME = 'fk_variants_product'),
              'DO 0',
              'ALTER TABLE multishop_catalog.variants ADD CONSTRAINT fk_variants_product FOREIGN KEY (product_id) REFERENCES multishop_catalog.products (id)');
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;

SET @sql = IF(EXISTS(SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
                     WHERE CONSTRAINT_SCHEMA = 'multishop_catalog' AND CONSTRAINT_NAME = 'fk_images_to_products_product'),
              'DO 0',
              'ALTER TABLE multishop_catalog.images_to_products ADD CONSTRAINT fk_images_to_products_product FOREIGN KEY (id_product) REFERENCES multishop_catalog.products (id)');
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;

SET @fk = (SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'product_items' AND REFERENCED_TABLE_NAME = 'products' LIMIT 1);
SET @sql = IF(@fk IS NULL, 'DO 0', CONCAT('ALTER TABLE product_items DROP FOREIGN KEY ', @fk));
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;
