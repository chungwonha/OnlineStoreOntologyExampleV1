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
INSERT INTO customers (id, name, email, total_purchases, order_count) VALUES
                                                                          ('c1', 'John Doe', 'john@example.com', 0, 0),
                                                                          ('c2', 'Jane Smith', 'jane@example.com', 0, 0),
                                                                          ('c3', 'Bob Johnson', 'bob@example.com', 0, 0),
                                                                          ('c4', 'Alice Brown', 'alice@example.com', 0, 0),
                                                                          ('c5', 'Charlie Wilson', 'charlie@example.com', 0, 0);

-- Populate Orders
INSERT INTO orders (id, customer_id, status, order_date) VALUES
-- John Doe's orders (will be premium)
('o1', 'c1', 'Completed', DATEADD('DAY', -30, CURRENT_TIMESTAMP)),
('o2', 'c1', 'Completed', DATEADD('DAY', -25, CURRENT_TIMESTAMP)),
('o3', 'c1', 'Completed', DATEADD('DAY', -20, CURRENT_TIMESTAMP)),
('o4', 'c1', 'Completed', DATEADD('DAY', -15, CURRENT_TIMESTAMP)),
('o5', 'c1', 'Completed', DATEADD('DAY', -10, CURRENT_TIMESTAMP)),
('o6', 'c1', 'Completed', DATEADD('DAY', -5, CURRENT_TIMESTAMP)),

-- Jane Smith's orders (will be premium)
('o7', 'c2', 'Completed', DATEADD('DAY', -28, CURRENT_TIMESTAMP)),
('o8', 'c2', 'Completed', DATEADD('DAY', -21, CURRENT_TIMESTAMP)),
('o9', 'c2', 'Completed', DATEADD('DAY', -14, CURRENT_TIMESTAMP)),
('o10', 'c2', 'Completed', DATEADD('DAY', -7, CURRENT_TIMESTAMP)),
('o11', 'c2', 'Completed', DATEADD('DAY', -1, CURRENT_TIMESTAMP)),

-- Bob Johnson's orders (not premium - high value but low count)
('o12', 'c3', 'Completed', DATEADD('DAY', -10, CURRENT_TIMESTAMP)),
('o13', 'c3', 'Completed', DATEADD('DAY', -5, CURRENT_TIMESTAMP)),

-- Alice Brown's orders (not premium - high count but low value)
('o14', 'c4', 'Completed', DATEADD('DAY', -30, CURRENT_TIMESTAMP)),
('o15', 'c4', 'Completed', DATEADD('DAY', -25, CURRENT_TIMESTAMP)),
('o16', 'c4', 'Completed', DATEADD('DAY', -20, CURRENT_TIMESTAMP)),
('o17', 'c4', 'Completed', DATEADD('DAY', -15, CURRENT_TIMESTAMP)),
('o18', 'c4', 'Completed', DATEADD('DAY', -10, CURRENT_TIMESTAMP)),

-- Charlie Wilson's orders (not premium - low count and low value)
('o19', 'c5', 'Completed', DATEADD('DAY', -20, CURRENT_TIMESTAMP)),
('o20', 'c5', 'Completed', DATEADD('DAY', -10, CURRENT_TIMESTAMP));

-- Populate Order Products
INSERT INTO order_products (order_id, product_id, quantity) VALUES
-- John Doe's order items
('o1', 'p1', 1),  -- Laptop
('o2', 'p2', 1),  -- Smartphone
('o3', 'p3', 2),  -- Headphones
('o4', 'p4', 3),  -- Running Shoes
('o5', 'p5', 2),  -- Yoga Mat
('o6', 'p6', 1),  -- Coffee Maker

-- Jane Smith's order items
('o7', 'p1', 1),  -- Laptop
('o8', 'p2', 1),  -- Smartphone
('o9', 'p3', 1),  -- Headphones
('o10', 'p6', 2), -- Coffee Maker
('o11', 'p7', 3), -- Blender

-- Bob Johnson's order items
('o12', 'p1', 1), -- Laptop
('o13', 'p2', 1), -- Smartphone

-- Alice Brown's order items
('o14', 'p5', 1), -- Yoga Mat
('o15', 'p5', 1), -- Yoga Mat
('o16', 'p5', 1), -- Yoga Mat
('o17', 'p5', 1), -- Yoga Mat
('o18', 'p5', 1), -- Yoga Mat

-- Charlie Wilson's order items
('o19', 'p4', 1), -- Running Shoes
('o20', 'p5', 1); -- Yoga Mat

-- Update customer total_purchases and order_count
UPDATE customers
SET total_purchases = (
    SELECT COALESCE(SUM(p.price * op.quantity), 0)
    FROM orders o
             JOIN order_products op ON o.id = op.order_id
             JOIN products p ON op.product_id = p.id
    WHERE o.customer_id = customers.id
),
    order_count = (
        SELECT COUNT(DISTINCT id)
        FROM orders
        WHERE customer_id = customers.id
    );