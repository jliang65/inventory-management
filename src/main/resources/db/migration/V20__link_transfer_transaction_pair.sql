-- Link TRANSFER_OUT to its paired TRANSFER_IN (seed V19 only set the reverse link).
UPDATE inventory_transactions
SET related_transaction_id = 11
WHERE id = 10
  AND transaction_type = 'TRANSFER_OUT'
  AND related_transaction_id IS NULL;
