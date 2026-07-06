ALTER TABLE products ADD CONSTRAINT chk_reorder_level_non_negative CHECK (reorder_level >= 0);
