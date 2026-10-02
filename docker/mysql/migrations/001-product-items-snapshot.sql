ALTER TABLE product_items
    ADD COLUMN product_name VARCHAR(255) NULL,
    ADD COLUMN unit_price BIGINT NULL;

UPDATE product_items pi
JOIN products p ON p.id = pi.product_id
SET pi.product_name = p.product_name,
    pi.unit_price = p.price
WHERE pi.unit_price IS NULL;
