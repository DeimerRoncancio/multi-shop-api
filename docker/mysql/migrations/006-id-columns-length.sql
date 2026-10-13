ALTER TABLE multishop_catalog.categories_to_products DROP FOREIGN KEY fk_categories_to_products_product;
ALTER TABLE multishop_catalog.categories_to_products DROP FOREIGN KEY fk_categories_to_products_category;
ALTER TABLE multishop_catalog.images_to_products DROP FOREIGN KEY fk_images_to_products_product;
ALTER TABLE multishop_catalog.variants DROP FOREIGN KEY fk_variants_product;

ALTER TABLE multishop_catalog.products MODIFY COLUMN id varchar(255) NOT NULL;
ALTER TABLE multishop_catalog.prod_categories MODIFY COLUMN id varchar(255) NOT NULL;
ALTER TABLE multishop_catalog.categories_to_products MODIFY COLUMN id_product varchar(255) NOT NULL;
ALTER TABLE multishop_catalog.categories_to_products MODIFY COLUMN id_category varchar(255) NOT NULL;
ALTER TABLE multishop_catalog.images_to_products MODIFY COLUMN id_product varchar(255) NOT NULL;
ALTER TABLE multishop_catalog.variants MODIFY COLUMN product_id varchar(255);

ALTER TABLE multishop_catalog.categories_to_products ADD CONSTRAINT fk_categories_to_products_product FOREIGN KEY (id_product) REFERENCES multishop_catalog.products (id);
ALTER TABLE multishop_catalog.categories_to_products ADD CONSTRAINT fk_categories_to_products_category FOREIGN KEY (id_category) REFERENCES multishop_catalog.prod_categories (id);
ALTER TABLE multishop_catalog.images_to_products ADD CONSTRAINT fk_images_to_products_product FOREIGN KEY (id_product) REFERENCES multishop_catalog.products (id);
ALTER TABLE multishop_catalog.variants ADD CONSTRAINT fk_variants_product FOREIGN KEY (product_id) REFERENCES multishop_catalog.products (id);

ALTER TABLE multishop_transactions.customer_addresses DROP FOREIGN KEY fk_customer_addresses_customer;
ALTER TABLE multishop_transactions.customer_addresses MODIFY COLUMN customer_id varchar(255);
ALTER TABLE multishop_transactions.customer_addresses ADD CONSTRAINT fk_customer_addresses_customer FOREIGN KEY (customer_id) REFERENCES multishop_transactions.customers (id);
