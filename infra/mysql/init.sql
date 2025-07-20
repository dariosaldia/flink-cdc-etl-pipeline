-- create the orders table in the inventory DB
CREATE TABLE orders (
  order_id INT AUTO_INCREMENT PRIMARY KEY,
  product_name VARCHAR(255) NOT NULL,
  quantity INT NOT NULL,
  order_date DATE NOT NULL
);

GRANT RELOAD, LOCK TABLES, REPLICATION SLAVE, REPLICATION CLIENT
  ON *.* TO 'debezium'@'%';
FLUSH PRIVILEGES;
