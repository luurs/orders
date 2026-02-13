CREATE TABLE orders_good
(
    orders_id BIGINT REFERENCES orders(orders_id),
    price NUMERIC(10, 2) NOT NULL,
    count NUMERIC(10, 2) CHECK (count > 0) NOT NULL,
    sum NUMERIC(10, 2) NOT NULL,
    name VARCHAR(255) NOT NULL,
    external_id VARCHAR(100) NOT NULL UNIQUE
);