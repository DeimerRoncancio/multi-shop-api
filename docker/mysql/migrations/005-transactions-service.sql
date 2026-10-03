CREATE DATABASE IF NOT EXISTS multishop_transactions;

CREATE TABLE IF NOT EXISTS multishop_transactions.guests LIKE guests;
CREATE TABLE IF NOT EXISTS multishop_transactions.customers LIKE customers;
CREATE TABLE IF NOT EXISTS multishop_transactions.customer_addresses LIKE customer_addresses;
CREATE TABLE IF NOT EXISTS multishop_transactions.transactions LIKE transactions;
CREATE TABLE IF NOT EXISTS multishop_transactions.product_items LIKE product_items;
CREATE TABLE IF NOT EXISTS multishop_transactions.transaction_address LIKE transaction_address;

INSERT IGNORE INTO multishop_transactions.guests SELECT * FROM guests;
INSERT IGNORE INTO multishop_transactions.customers SELECT * FROM customers;
INSERT IGNORE INTO multishop_transactions.customer_addresses SELECT * FROM customer_addresses;
INSERT IGNORE INTO multishop_transactions.transactions SELECT * FROM transactions;
INSERT IGNORE INTO multishop_transactions.product_items SELECT * FROM product_items;
INSERT IGNORE INTO multishop_transactions.transaction_address SELECT * FROM transaction_address;

SET @sql = IF(EXISTS(SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
                     WHERE CONSTRAINT_SCHEMA = 'multishop_transactions' AND CONSTRAINT_NAME = 'fk_customers_guest'),
              'DO 0',
              'ALTER TABLE multishop_transactions.customers ADD CONSTRAINT fk_customers_guest FOREIGN KEY (guest_id) REFERENCES multishop_transactions.guests (id)');
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;

SET @sql = IF(EXISTS(SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
                     WHERE CONSTRAINT_SCHEMA = 'multishop_transactions' AND CONSTRAINT_NAME = 'fk_customer_addresses_customer'),
              'DO 0',
              'ALTER TABLE multishop_transactions.customer_addresses ADD CONSTRAINT fk_customer_addresses_customer FOREIGN KEY (customer_id) REFERENCES multishop_transactions.customers (id)');
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;

SET @sql = IF(EXISTS(SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
                     WHERE CONSTRAINT_SCHEMA = 'multishop_transactions' AND CONSTRAINT_NAME = 'fk_transactions_customer'),
              'DO 0',
              'ALTER TABLE multishop_transactions.transactions ADD CONSTRAINT fk_transactions_customer FOREIGN KEY (customer_id) REFERENCES multishop_transactions.customers (id)');
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;

SET @sql = IF(EXISTS(SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
                     WHERE CONSTRAINT_SCHEMA = 'multishop_transactions' AND CONSTRAINT_NAME = 'fk_product_items_transaction'),
              'DO 0',
              'ALTER TABLE multishop_transactions.product_items ADD CONSTRAINT fk_product_items_transaction FOREIGN KEY (transaction_id) REFERENCES multishop_transactions.transactions (id)');
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;

SET @sql = IF(EXISTS(SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
                     WHERE CONSTRAINT_SCHEMA = 'multishop_transactions' AND CONSTRAINT_NAME = 'fk_transaction_address_transaction'),
              'DO 0',
              'ALTER TABLE multishop_transactions.transaction_address ADD CONSTRAINT fk_transaction_address_transaction FOREIGN KEY (transaction_id) REFERENCES multishop_transactions.transactions (id)');
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;
