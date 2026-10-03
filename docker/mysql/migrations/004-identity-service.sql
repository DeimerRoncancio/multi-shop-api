CREATE DATABASE IF NOT EXISTS multishop_identity;

CREATE TABLE IF NOT EXISTS multishop_identity.users LIKE users;
CREATE TABLE IF NOT EXISTS multishop_identity.roles LIKE roles;
CREATE TABLE IF NOT EXISTS multishop_identity.roles_to_users LIKE roles_to_users;

INSERT IGNORE INTO multishop_identity.users SELECT * FROM users;
INSERT IGNORE INTO multishop_identity.roles SELECT * FROM roles;
INSERT IGNORE INTO multishop_identity.roles_to_users SELECT * FROM roles_to_users;

SET @sql = IF(EXISTS(SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
                     WHERE CONSTRAINT_SCHEMA = 'multishop_identity' AND CONSTRAINT_NAME = 'fk_roles_to_users_user'),
              'DO 0',
              'ALTER TABLE multishop_identity.roles_to_users ADD CONSTRAINT fk_roles_to_users_user FOREIGN KEY (id_user) REFERENCES multishop_identity.users (id)');
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;

SET @sql = IF(EXISTS(SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
                     WHERE CONSTRAINT_SCHEMA = 'multishop_identity' AND CONSTRAINT_NAME = 'fk_roles_to_users_role'),
              'DO 0',
              'ALTER TABLE multishop_identity.roles_to_users ADD CONSTRAINT fk_roles_to_users_role FOREIGN KEY (id_role) REFERENCES multishop_identity.roles (id)');
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;

SET @fk = (SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'customers' AND REFERENCED_TABLE_NAME = 'users' LIMIT 1);
SET @sql = IF(@fk IS NULL, 'DO 0', CONCAT('ALTER TABLE customers DROP FOREIGN KEY ', @fk));
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;
