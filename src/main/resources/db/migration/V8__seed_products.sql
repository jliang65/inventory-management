-- Electronics (category_id: 1, supplier_id: 1)
INSERT INTO products (sku, name, description, category_id, supplier_id, unit_price, reorder_level, active) VALUES
    ('ELEC-TV-SAM55X', 'Samsung 55" 4K Smart TV', 'Crystal UHD display with HDR support and built-in streaming apps', 1, 1, 549.99, 5, TRUE),
    ('ELEC-HP-SONY-WH', 'Sony WH-1000XM5 Headphones', 'Wireless noise-canceling over-ear headphones with 30-hour battery', 1, 1, 349.99, 10, TRUE),
    ('ELEC-KB-LOG-MX', 'Logitech MX Keys Keyboard', 'Wireless illuminated keyboard with smart backlighting', 1, 1, 119.99, 15, TRUE),
    ('ELEC-MON-LG27', 'LG 27" UltraGear Gaming Monitor', '144Hz IPS display with G-Sync compatibility', 1, 1, 299.99, 8, TRUE),
    ('ELEC-CHG-ANK-65W', 'Anker 65W USB-C Charger', 'GaN fast charger with dual USB-C ports', 1, 1, 45.99, 25, TRUE);

-- Clothing (category_id: 2, supplier_id: 2)
INSERT INTO products (sku, name, description, category_id, supplier_id, unit_price, reorder_level, active) VALUES
    ('CLO-TSH-BLK-M', 'Classic Black T-Shirt (M)', '100% cotton crew neck t-shirt, medium', 2, 2, 24.99, 20, TRUE),
    ('CLO-TSH-BLK-L', 'Classic Black T-Shirt (L)', '100% cotton crew neck t-shirt, large', 2, 2, 24.99, 20, TRUE),
    ('CLO-DNM-SLM-32', 'Slim Fit Denim Jeans (32)', 'Stretch denim with slim fit, waist 32"', 2, 2, 59.99, 15, TRUE),
    ('CLO-JKT-WND-M', 'Windbreaker Jacket (M)', 'Lightweight water-resistant windbreaker, medium', 2, 2, 79.99, 10, TRUE),
    ('CLO-SNK-WHT-10', 'Canvas Sneakers White (10)', 'Classic low-top canvas sneakers, size 10', 2, 2, 49.99, 12, TRUE);

-- Books (category_id: 3, supplier_id: 3)
INSERT INTO products (sku, name, description, category_id, supplier_id, unit_price, reorder_level, active) VALUES
    ('BK-978-1681375663', 'When We Cease to Understand the World', 'Benjamín Labatut - A haunting exploration of scientific obsession and the boundaries of human knowledge', 3, 3, 16.99, 10, TRUE),
    ('BK-978-0143127550', 'Sapiens: A Brief History of Humankind', 'Yuval Noah Harari - A groundbreaking narrative of humanity''s creation and evolution', 3, 3, 18.99, 15, TRUE),
    ('BK-978-0374533557', 'Thinking, Fast and Slow', 'Daniel Kahneman - A tour of the mind explaining two systems that drive the way we think', 3, 3, 17.99, 12, TRUE),
    ('BK-978-0525559474', 'The Midnight Library', 'Matt Haig - A novel about all the choices that go into a life well lived', 3, 3, 14.99, 20, TRUE),
    ('BK-978-0593135204', 'Project Hail Mary', 'Andy Weir - A lone astronaut must save Earth from disaster in this interstellar adventure', 3, 3, 19.99, 18, TRUE);
