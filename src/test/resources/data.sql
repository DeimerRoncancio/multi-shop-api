INSERT INTO roles (id, role) VALUES
    ('rol-usuario', 'ROLE_USER'),
    ('rol-admin', 'ROLE_ADMIN');

ALTER TABLE product_items ADD CONSTRAINT fk_product_items_product FOREIGN KEY (product_id) REFERENCES products (id);
ALTER TABLE customers ADD CONSTRAINT fk_customers_user_email FOREIGN KEY (user_email) REFERENCES users (email);
