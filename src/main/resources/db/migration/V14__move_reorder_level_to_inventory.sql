-- Add reorder_level to inventory table
ALTER TABLE inventory ADD COLUMN reorder_level INTEGER NOT NULL DEFAULT 0;

-- Copy reorder_level from products to inventory for existing records
UPDATE inventory i
SET reorder_level = p.reorder_level
FROM products p
WHERE i.product_id = p.id;

-- Remove reorder_level from products table
ALTER TABLE products DROP COLUMN reorder_level;
