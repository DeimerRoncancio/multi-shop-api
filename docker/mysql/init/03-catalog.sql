CREATE DATABASE IF NOT EXISTS multishop_catalog;

CREATE USER IF NOT EXISTS 'catalog'@'%' IDENTIFIED BY 'catalog';
GRANT ALL PRIVILEGES ON multishop_catalog.* TO 'catalog'@'%';
