DROP DATABASE IF EXISTS price_comparator;
CREATE DATABASE price_comparator;
USE price_comparator;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE products (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(255),
    specification VARCHAR(255),
    barcode VARCHAR(50),
    image_url TEXT,
    platform VARCHAR(50),
    link TEXT,
    price DECIMAL(10, 2)
);

CREATE TABLE prices (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id VARCHAR(50),
    price DECIMAL(10, 2),
    checked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE search_records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    name VARCHAR(255) NOT NULL,
    foreign key (user_id) REFERENCES users(id)
);

CREATE TABLE favorites(
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100),
    product_id VARCHAR(50),
    foreign key (email) REFERENCES users(email),
    foreign key (product_id) REFERENCES products(id)
)