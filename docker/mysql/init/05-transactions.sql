CREATE DATABASE IF NOT EXISTS multishop_transactions;

CREATE USER IF NOT EXISTS 'transactions'@'%' IDENTIFIED BY 'transactions';
GRANT ALL PRIVILEGES ON multishop_transactions.* TO 'transactions'@'%';
