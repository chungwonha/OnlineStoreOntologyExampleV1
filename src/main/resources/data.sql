-- Populate Products
INSERT INTO products (id, name, category, price) VALUES
                                                     ('p1', 'Laptop', 'Electronics', 999.99),
                                                     ('p2', 'Smartphone', 'Electronics', 599.99),
                                                     ('p3', 'Headphones', 'Electronics', 199.99),
                                                     ('p4', 'Running Shoes', 'Sports', 89.99),
                                                     ('p5', 'Yoga Mat', 'Sports', 29.99),
                                                     ('p6', 'Coffee Maker', 'Home Appliances', 79.99),
                                                     ('p7', 'Blender', 'Home Appliances', 49.99);

-- Populate Customers
INSERT INTO customers (id, name, email) VALUES
                                            ('c1', 'John Doe', 'john@example.com'),
                                            ('c2', 'Jane Smith', 'jane@example.com'),
                                            ('c3', 'Bob Johnson', 'bob@example.com');

-- Populate Orders
INSERT INTO orders (id, customer_id, status, order_date) VALUES
                                                             ('o1', 'c1', 'Shipped', CURRENT_TIMESTAMP - 7),
                                                             ('o2', 'c2', 'Processing', CURRENT_TIMESTAMP - 3),
                                                             ('o3', 'c3', 'Delivered', CURRENT_TIMESTAMP - 14),
                                                             ('o4', 'c1', 'Processing', CURRENT_TIMESTAMP - 1);

-- Populate Order Products
INSERT INTO order_products (order_id, product_id, quantity) VALUES
                                                                ('o1', 'p1', 1),
                                                                ('o1', 'p3', 1),
                                                                ('o2', 'p2', 1),
                                                                ('o2', 'p4', 2),
                                                                ('o3', 'p6', 1),
                                                                ('o3', 'p7', 1),
                                                                ('o4', 'p5', 3);