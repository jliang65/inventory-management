-- Create some low stock scenarios for testing the /low-stock endpoint
-- Low stock = quantity <= reorder_level

-- KC Warehouse: Samsung TV (reorder_level=20) - reduce from 50 to 18
UPDATE inventory SET quantity = 18 WHERE location_id = 1 AND product_id = 1;

-- KC Warehouse: Windbreaker (reorder_level=40) - reduce from 120 to 35
UPDATE inventory SET quantity = 35 WHERE location_id = 1 AND product_id = 9;

-- Dallas Warehouse: LG Monitor (reorder_level=15) - reduce from 45 to 12
UPDATE inventory SET quantity = 12 WHERE location_id = 2 AND product_id = 4;

-- Dallas Warehouse: Thinking Fast and Slow (reorder_level=20) - reduce from 55 to 15
UPDATE inventory SET quantity = 15 WHERE location_id = 2 AND product_id = 13;

-- Overland Park Store: Sony Headphones (reorder_level=4) - reduce from 12 to 3
UPDATE inventory SET quantity = 3 WHERE location_id = 3 AND product_id = 2;

-- Overland Park Store: Denim Jeans (reorder_level=8) - reduce from 25 to 6
UPDATE inventory SET quantity = 6 WHERE location_id = 3 AND product_id = 8;

-- Phoenix Service Center: Logitech Keyboard (reorder_level=3) - reduce from 8 to 2
UPDATE inventory SET quantity = 2 WHERE location_id = 4 AND product_id = 3;
