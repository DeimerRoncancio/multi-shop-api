INSERT INTO roles (id, role) VALUES
    ('rol-usuario', 'ROLE_USER'),
    ('rol-admin', 'ROLE_ADMIN');

ALTER TABLE customers ADD CONSTRAINT fk_customers_user_email FOREIGN KEY (user_email) REFERENCES users (email);
