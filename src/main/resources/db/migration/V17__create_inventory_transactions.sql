CREATE TABLE inventory_transactions (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    location_id BIGINT NOT NULL,
    transaction_type VARCHAR(30) NOT NULL,
    quantity INT NOT NULL,
    previous_quantity INT NOT NULL,
    new_quantity INT NOT NULL,
    related_transaction_id BIGINT,
    reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (location_id) REFERENCES locations(id),
    FOREIGN KEY (related_transaction_id) REFERENCES inventory_transactions(id),

    CONSTRAINT chk_inventory_transaction_quantity_positive CHECK (quantity > 0),
    CONSTRAINT chk_inventory_transaction_type CHECK (
        transaction_type IN (
            'STOCK_IN',
            'STOCK_OUT',
            'ADJUSTMENT',
            'TRANSFER_IN',
            'TRANSFER_OUT'
        )
    )
);
