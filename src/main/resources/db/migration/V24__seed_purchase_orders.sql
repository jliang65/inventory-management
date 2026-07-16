-- Draft purchase orders for testing

-- Order 1: Low stock restock from Acme Electronics to KC Warehouse
INSERT INTO purchase_orders (supplier_id, destination_location_id, status, expected_delivery_date, notes)
VALUES (1, 1, 'DRAFT', '2026-08-01', 'Restocking low inventory items');

-- Order 2: Low stock restock from Global Textiles to Overland Park Store
INSERT INTO purchase_orders (supplier_id, destination_location_id, status, expected_delivery_date, notes)
VALUES (2, 3, 'DRAFT', '2026-08-10', 'Restocking low inventory items');

-- Order 3: Routine restock from BookWorld to Dallas Warehouse
INSERT INTO purchase_orders (supplier_id, destination_location_id, status, expected_delivery_date, notes)
VALUES (3, 2, 'DRAFT', '2026-08-15', 'Monthly book restock');

-- Line items for Order 1 (Acme Electronics → KC Warehouse)
INSERT INTO purchase_order_items (purchase_order_id, product_id, ordered_quantity, unit_cost)
VALUES
    (1, 1, 30, 549.99),  -- Samsung TV
    (1, 4, 20, 299.99);  -- LG Monitor

-- Line items for Order 2 (Global Textiles → Overland Park Store)
INSERT INTO purchase_order_items (purchase_order_id, product_id, ordered_quantity, unit_cost)
VALUES
    (2, 8, 50, 59.99),   -- Denim Jeans
    (2, 9, 40, 79.99);   -- Windbreaker

-- Line items for Order 3 (BookWorld → Dallas Warehouse)
INSERT INTO purchase_order_items (purchase_order_id, product_id, ordered_quantity, unit_cost)
VALUES
    (3, 12, 25, 18.99),  -- Sapiens
    (3, 15, 30, 19.99);  -- Project Hail Mary
