-- Replace seeded transactions so history aligns with post-V16 inventory quantities.
DELETE FROM inventory_transactions;

INSERT INTO inventory_transactions
    (product_id, location_id, transaction_type, quantity, previous_quantity, new_quantity, reason, created_at)
VALUES
    -- Kansas City Warehouse
    (1, 1, 'STOCK_IN', 50, 0, 50, 'Initial warehouse receipt', '2026-01-10 08:30:00'),
    (1, 1, 'ADJUSTMENT', 32, 50, 18, 'Low stock adjustment', '2026-02-01 09:00:00'),
    (2, 1, 'STOCK_IN', 100, 0, 100, 'Initial warehouse receipt', '2026-01-10 08:35:00'),
    (3, 1, 'STOCK_IN', 150, 0, 150, 'Initial warehouse receipt', '2026-01-10 08:40:00'),
    (5, 1, 'TRANSFER_OUT', 25, 225, 200, 'Transfer to Dallas warehouse', '2026-02-10 09:15:00'),
    (6, 1, 'STOCK_IN', 300, 0, 300, 'Initial warehouse receipt', '2026-01-11 10:00:00'),
    (9, 1, 'STOCK_IN', 120, 0, 120, 'Initial warehouse receipt', '2026-01-12 11:00:00'),
    (9, 1, 'ADJUSTMENT', 85, 120, 35, 'Low stock adjustment', '2026-02-02 09:30:00'),

    -- Dallas Warehouse
    (4, 2, 'STOCK_IN', 45, 0, 45, 'Initial warehouse receipt', '2026-01-13 08:00:00'),
    (4, 2, 'ADJUSTMENT', 33, 45, 12, 'Low stock adjustment', '2026-02-03 10:00:00'),
    (13, 2, 'STOCK_IN', 55, 0, 55, 'Initial warehouse receipt', '2026-01-14 08:30:00'),
    (13, 2, 'ADJUSTMENT', 40, 55, 15, 'Low stock adjustment', '2026-02-04 11:00:00'),
    (15, 2, 'STOCK_IN', 130, 0, 130, 'Publisher restock', '2026-02-18 08:50:00'),

    -- Overland Park Retail Store
    (1, 3, 'STOCK_IN', 5, 0, 5, 'Floor stock setup', '2026-01-15 09:00:00'),
    (2, 3, 'STOCK_IN', 12, 0, 12, 'Floor stock setup', '2026-01-15 09:15:00'),
    (2, 3, 'STOCK_OUT', 9, 12, 3, 'Sales period', '2026-02-08 14:22:00'),
    (8, 3, 'STOCK_IN', 25, 0, 25, 'Floor stock setup', '2026-01-16 10:00:00'),
    (8, 3, 'ADJUSTMENT', 19, 25, 6, 'Low stock adjustment', '2026-02-09 16:00:00'),
    (11, 3, 'STOCK_IN', 10, 0, 10, 'New title shipment', '2026-02-12 10:00:00'),

    -- Kansas City Returns Center
    (14, 5, 'STOCK_IN', 4, 0, 4, 'Customer return intake', '2026-02-15 13:30:00'),

    -- Phoenix Service Center
    (3, 4, 'STOCK_IN', 8, 0, 8, 'Replacement parts stock', '2026-01-20 09:00:00'),
    (3, 4, 'ADJUSTMENT', 6, 8, 2, 'Damaged unit write-down', '2026-02-20 11:20:00'),

    -- Overflow Storage
    (12, 7, 'STOCK_IN', 200, 0, 200, 'Bulk purchase overflow storage', '2026-03-01 07:45:00');

INSERT INTO inventory_transactions
    (product_id, location_id, transaction_type, quantity, previous_quantity, new_quantity, related_transaction_id, reason, created_at)
SELECT
    5,
    2,
    'TRANSFER_IN',
    25,
    95,
    120,
    out_tx.id,
    'Transfer from Kansas City warehouse',
    TIMESTAMP '2026-02-10 09:15:00'
FROM inventory_transactions out_tx
WHERE out_tx.product_id = 5
  AND out_tx.location_id = 1
  AND out_tx.transaction_type = 'TRANSFER_OUT'
  AND out_tx.created_at = TIMESTAMP '2026-02-10 09:15:00';

UPDATE inventory_transactions out_tx
SET related_transaction_id = in_tx.id
FROM inventory_transactions in_tx
WHERE out_tx.transaction_type = 'TRANSFER_OUT'
  AND in_tx.transaction_type = 'TRANSFER_IN'
  AND out_tx.product_id = in_tx.product_id
  AND out_tx.quantity = in_tx.quantity
  AND out_tx.created_at = in_tx.created_at
  AND out_tx.related_transaction_id IS NULL
  AND in_tx.related_transaction_id = out_tx.id;

SELECT setval(
    pg_get_serial_sequence('inventory_transactions', 'id'),
    (SELECT COALESCE(MAX(id), 1) FROM inventory_transactions)
);
