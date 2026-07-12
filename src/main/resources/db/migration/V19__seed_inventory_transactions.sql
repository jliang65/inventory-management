-- Sample transaction history for development and testing.
-- Quantities represent historical movements, not current inventory balances.

INSERT INTO inventory_transactions
    (product_id, location_id, transaction_type, quantity, previous_quantity, new_quantity, reason, created_at)
VALUES
    (1, 1, 'STOCK_IN', 50, 0, 50, 'Initial warehouse receipt', '2026-01-10 08:30:00'),
    (2, 1, 'STOCK_IN', 100, 0, 100, 'Initial warehouse receipt', '2026-01-10 08:35:00'),
    (3, 1, 'STOCK_IN', 150, 0, 150, 'Initial warehouse receipt', '2026-01-10 08:40:00'),
    (1, 3, 'STOCK_OUT', 2, 7, 5, 'Retail floor replenishment', '2026-01-18 11:15:00'),
    (2, 3, 'STOCK_OUT', 3, 15, 12, 'Customer sale', '2026-01-20 14:22:00'),
    (4, 2, 'ADJUSTMENT', 3, 48, 45, 'Cycle count correction', '2026-02-01 09:00:00'),
    (6, 1, 'STOCK_OUT', 20, 320, 300, 'Store allocation', '2026-02-05 16:45:00'),
    (11, 3, 'STOCK_IN', 10, 0, 10, 'New title shipment', '2026-02-12 10:00:00'),
    (14, 5, 'STOCK_IN', 4, 0, 4, 'Customer return intake', '2026-02-15 13:30:00'),
    (5, 1, 'TRANSFER_OUT', 25, 200, 175, 'Transfer to Dallas warehouse', '2026-02-20 09:15:00');

INSERT INTO inventory_transactions
    (product_id, location_id, transaction_type, quantity, previous_quantity, new_quantity, related_transaction_id, reason, created_at)
VALUES
    (5, 2, 'TRANSFER_IN', 25, 120, 145, 10, 'Transfer from Kansas City warehouse', '2026-02-20 09:15:00');

INSERT INTO inventory_transactions
    (product_id, location_id, transaction_type, quantity, previous_quantity, new_quantity, reason, created_at)
VALUES
    (12, 7, 'STOCK_IN', 200, 0, 200, 'Bulk purchase overflow storage', '2026-03-01 07:45:00'),
    (8, 3, 'STOCK_OUT', 5, 30, 25, 'Weekend sale', '2026-03-08 18:00:00'),
    (3, 4, 'ADJUSTMENT', 2, 10, 8, 'Damaged unit write-down', '2026-03-12 11:20:00'),
    (15, 2, 'STOCK_IN', 30, 100, 130, 'Publisher restock', '2026-03-20 08:50:00');

SELECT setval(
    pg_get_serial_sequence('inventory_transactions', 'id'),
    (SELECT COALESCE(MAX(id), 1) FROM inventory_transactions)
);
