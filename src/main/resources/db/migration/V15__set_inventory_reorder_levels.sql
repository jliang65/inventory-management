-- Set reorder levels based on facility type and product characteristics

-- Kansas City Warehouse (location_id=1) - Primary warehouse, high reorder levels
UPDATE inventory SET reorder_level = 20 WHERE location_id = 1 AND product_id = 1;   -- Samsung TV
UPDATE inventory SET reorder_level = 30 WHERE location_id = 1 AND product_id = 2;   -- Sony Headphones
UPDATE inventory SET reorder_level = 50 WHERE location_id = 1 AND product_id = 3;   -- Logitech Keyboard
UPDATE inventory SET reorder_level = 25 WHERE location_id = 1 AND product_id = 4;   -- LG Monitor
UPDATE inventory SET reorder_level = 75 WHERE location_id = 1 AND product_id = 5;   -- Anker Charger
UPDATE inventory SET reorder_level = 100 WHERE location_id = 1 AND product_id = 6;  -- Black T-Shirt M
UPDATE inventory SET reorder_level = 100 WHERE location_id = 1 AND product_id = 7;  -- Black T-Shirt L
UPDATE inventory SET reorder_level = 60 WHERE location_id = 1 AND product_id = 8;   -- Denim Jeans
UPDATE inventory SET reorder_level = 40 WHERE location_id = 1 AND product_id = 9;   -- Windbreaker
UPDATE inventory SET reorder_level = 50 WHERE location_id = 1 AND product_id = 10;  -- Canvas Sneakers
UPDATE inventory SET reorder_level = 25 WHERE location_id = 1 AND product_id = 11;  -- When We Cease to Understand
UPDATE inventory SET reorder_level = 40 WHERE location_id = 1 AND product_id = 12;  -- Sapiens
UPDATE inventory SET reorder_level = 30 WHERE location_id = 1 AND product_id = 13;  -- Thinking Fast and Slow
UPDATE inventory SET reorder_level = 50 WHERE location_id = 1 AND product_id = 14;  -- Midnight Library
UPDATE inventory SET reorder_level = 75 WHERE location_id = 1 AND product_id = 15;  -- Project Hail Mary

-- Dallas Warehouse (location_id=2) - Secondary warehouse, moderate-high reorder levels
UPDATE inventory SET reorder_level = 15 WHERE location_id = 2 AND product_id = 1;   -- Samsung TV
UPDATE inventory SET reorder_level = 20 WHERE location_id = 2 AND product_id = 2;   -- Sony Headphones
UPDATE inventory SET reorder_level = 30 WHERE location_id = 2 AND product_id = 3;   -- Logitech Keyboard
UPDATE inventory SET reorder_level = 15 WHERE location_id = 2 AND product_id = 4;   -- LG Monitor
UPDATE inventory SET reorder_level = 40 WHERE location_id = 2 AND product_id = 5;   -- Anker Charger
UPDATE inventory SET reorder_level = 60 WHERE location_id = 2 AND product_id = 6;   -- Black T-Shirt M
UPDATE inventory SET reorder_level = 60 WHERE location_id = 2 AND product_id = 7;   -- Black T-Shirt L
UPDATE inventory SET reorder_level = 35 WHERE location_id = 2 AND product_id = 8;   -- Denim Jeans
UPDATE inventory SET reorder_level = 25 WHERE location_id = 2 AND product_id = 9;   -- Windbreaker
UPDATE inventory SET reorder_level = 30 WHERE location_id = 2 AND product_id = 10;  -- Canvas Sneakers
UPDATE inventory SET reorder_level = 15 WHERE location_id = 2 AND product_id = 11;  -- When We Cease to Understand
UPDATE inventory SET reorder_level = 25 WHERE location_id = 2 AND product_id = 12;  -- Sapiens
UPDATE inventory SET reorder_level = 20 WHERE location_id = 2 AND product_id = 13;  -- Thinking Fast and Slow
UPDATE inventory SET reorder_level = 35 WHERE location_id = 2 AND product_id = 14;  -- Midnight Library
UPDATE inventory SET reorder_level = 45 WHERE location_id = 2 AND product_id = 15;  -- Project Hail Mary

-- Overland Park Retail Store (location_id=3) - Small floor space, low reorder levels
UPDATE inventory SET reorder_level = 2 WHERE location_id = 3 AND product_id = 1;    -- Samsung TV
UPDATE inventory SET reorder_level = 4 WHERE location_id = 3 AND product_id = 2;    -- Sony Headphones
UPDATE inventory SET reorder_level = 5 WHERE location_id = 3 AND product_id = 3;    -- Logitech Keyboard
UPDATE inventory SET reorder_level = 3 WHERE location_id = 3 AND product_id = 4;    -- LG Monitor
UPDATE inventory SET reorder_level = 8 WHERE location_id = 3 AND product_id = 5;    -- Anker Charger
UPDATE inventory SET reorder_level = 12 WHERE location_id = 3 AND product_id = 6;   -- Black T-Shirt M
UPDATE inventory SET reorder_level = 12 WHERE location_id = 3 AND product_id = 7;   -- Black T-Shirt L
UPDATE inventory SET reorder_level = 8 WHERE location_id = 3 AND product_id = 8;    -- Denim Jeans
UPDATE inventory SET reorder_level = 6 WHERE location_id = 3 AND product_id = 9;    -- Windbreaker
UPDATE inventory SET reorder_level = 6 WHERE location_id = 3 AND product_id = 10;   -- Canvas Sneakers
UPDATE inventory SET reorder_level = 3 WHERE location_id = 3 AND product_id = 11;   -- When We Cease to Understand
UPDATE inventory SET reorder_level = 5 WHERE location_id = 3 AND product_id = 12;   -- Sapiens
UPDATE inventory SET reorder_level = 4 WHERE location_id = 3 AND product_id = 13;   -- Thinking Fast and Slow
UPDATE inventory SET reorder_level = 6 WHERE location_id = 3 AND product_id = 14;   -- Midnight Library
UPDATE inventory SET reorder_level = 8 WHERE location_id = 3 AND product_id = 15;   -- Project Hail Mary

-- Phoenix Service Center (location_id=4) - Replacement parts only, minimal levels
UPDATE inventory SET reorder_level = 2 WHERE location_id = 4 AND product_id = 2;    -- Sony Headphones
UPDATE inventory SET reorder_level = 3 WHERE location_id = 4 AND product_id = 3;    -- Logitech Keyboard
UPDATE inventory SET reorder_level = 5 WHERE location_id = 4 AND product_id = 5;    -- Anker Charger

-- Kansas City Returns Center (location_id=5) - No automatic reordering
UPDATE inventory SET reorder_level = 0 WHERE location_id = 5;

-- Damaged Goods Holding (location_id=6) - No automatic reordering
UPDATE inventory SET reorder_level = 0 WHERE location_id = 6;

-- Overflow Storage (location_id=7) - Bulk storage, moderate reorder levels
UPDATE inventory SET reorder_level = 150 WHERE location_id = 7 AND product_id = 5;  -- Anker Charger
UPDATE inventory SET reorder_level = 75 WHERE location_id = 7 AND product_id = 12;  -- Sapiens
UPDATE inventory SET reorder_level = 100 WHERE location_id = 7 AND product_id = 15; -- Project Hail Mary
