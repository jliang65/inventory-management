ALTER TABLE inventory_transactions
ADD COLUMN performed_by_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL;
