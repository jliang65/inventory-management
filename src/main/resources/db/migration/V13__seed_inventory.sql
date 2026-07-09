-- Inventory seed data
-- Locations: 1=KC Warehouse, 2=Dallas Warehouse, 3=Overland Park Store, 4=Phoenix Service, 5=KC Returns, 6=Damaged Goods, 7=Overflow Storage

-- Kansas City Warehouse (primary warehouse - high stock)
INSERT INTO inventory (product_id, location_id, quantity) VALUES
    (1, 1, 50),   -- Samsung TV
    (2, 1, 100),  -- Sony Headphones
    (3, 1, 150),  -- Logitech Keyboard
    (4, 1, 75),   -- LG Monitor
    (5, 1, 200),  -- Anker Charger
    (6, 1, 300),  -- Black T-Shirt M
    (7, 1, 280),  -- Black T-Shirt L
    (8, 1, 180),  -- Denim Jeans
    (9, 1, 120),  -- Windbreaker
    (10, 1, 160), -- Canvas Sneakers
    (11, 1, 80),  -- When We Cease to Understand
    (12, 1, 120), -- Sapiens
    (13, 1, 90),  -- Thinking Fast and Slow
    (14, 1, 150), -- Midnight Library
    (15, 1, 200); -- Project Hail Mary

-- Dallas Warehouse (secondary warehouse - moderate stock)
INSERT INTO inventory (product_id, location_id, quantity) VALUES
    (1, 2, 30),   -- Samsung TV
    (2, 2, 60),   -- Sony Headphones
    (3, 2, 80),   -- Logitech Keyboard
    (4, 2, 45),   -- LG Monitor
    (5, 2, 120),  -- Anker Charger
    (6, 2, 200),  -- Black T-Shirt M
    (7, 2, 180),  -- Black T-Shirt L
    (8, 2, 100),  -- Denim Jeans
    (9, 2, 70),   -- Windbreaker
    (10, 2, 90),  -- Canvas Sneakers
    (11, 2, 50),  -- When We Cease to Understand
    (12, 2, 75),  -- Sapiens
    (13, 2, 55),  -- Thinking Fast and Slow
    (14, 2, 100), -- Midnight Library
    (15, 2, 130); -- Project Hail Mary

-- Overland Park Retail Store (floor stock)
INSERT INTO inventory (product_id, location_id, quantity) VALUES
    (1, 3, 5),    -- Samsung TV
    (2, 3, 12),   -- Sony Headphones
    (3, 3, 15),   -- Logitech Keyboard
    (4, 3, 8),    -- LG Monitor
    (5, 3, 25),   -- Anker Charger
    (6, 3, 40),   -- Black T-Shirt M
    (7, 3, 35),   -- Black T-Shirt L
    (8, 3, 25),   -- Denim Jeans
    (9, 3, 18),   -- Windbreaker
    (10, 3, 20),  -- Canvas Sneakers
    (11, 3, 10),  -- When We Cease to Understand
    (12, 3, 15),  -- Sapiens
    (13, 3, 12),  -- Thinking Fast and Slow
    (14, 3, 20),  -- Midnight Library
    (15, 3, 25);  -- Project Hail Mary

-- Phoenix Service Center (repair/replacement parts)
INSERT INTO inventory (product_id, location_id, quantity) VALUES
    (2, 4, 5),    -- Sony Headphones (replacements)
    (3, 4, 8),    -- Logitech Keyboard (replacements)
    (5, 4, 15);   -- Anker Charger (replacements)

-- Kansas City Returns Center
INSERT INTO inventory (product_id, location_id, quantity) VALUES
    (1, 5, 3),    -- Samsung TV
    (2, 5, 7),    -- Sony Headphones
    (6, 5, 12),   -- Black T-Shirt M
    (8, 5, 5),    -- Denim Jeans
    (14, 5, 4);   -- Midnight Library

-- Damaged Goods Holding
INSERT INTO inventory (product_id, location_id, quantity) VALUES
    (1, 6, 2),    -- Samsung TV
    (4, 6, 1),    -- LG Monitor
    (10, 6, 3);   -- Canvas Sneakers

-- Overflow Storage (excess inventory)
INSERT INTO inventory (product_id, location_id, quantity) VALUES
    (5, 7, 500),  -- Anker Charger (bulk purchase)
    (12, 7, 200), -- Sapiens (bulk purchase)
    (15, 7, 300); -- Project Hail Mary (bulk purchase)
