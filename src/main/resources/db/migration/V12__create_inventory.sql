CREATE TABLE inventory (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    location_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 0,

    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (location_id) REFERENCES locations(id),
    
    CONSTRAINT uq_inventory_product_location UNIQUE (product_id, location_id),
    CONSTRAINT chk_inventory_quantity_nonnegative CHECK (quantity >= 0)
);
