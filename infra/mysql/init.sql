-- create the orders table in the inventory DB
CREATE TABLE orders (
  order_id INT AUTO_INCREMENT PRIMARY KEY,
  product_name VARCHAR(255) NOT NULL,
  quantity INT NOT NULL,
  order_date DATE NOT NULL
);

-- add a sample row
INSERT INTO orders (product_name, quantity, order_date)
VALUES ('Sample Item', 1, CURDATE());
