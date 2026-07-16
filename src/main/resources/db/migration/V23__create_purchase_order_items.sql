CREATE TABLE purchase_order_items (
    id BIGSERIAL PRIMARY KEY,
    purchase_order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    ordered_quantity INT NOT NULL,
    unit_cost DECIMAL(10, 2) NOT NULL,

    CONSTRAINT fk_item_purchase_order
        FOREIGN KEY (purchase_order_id)
        REFERENCES purchase_orders(id),

    CONSTRAINT fk_item_product
        FOREIGN KEY (product_id)
        REFERENCES products(id),

    CONSTRAINT positive_ordered_quantity
        CHECK (ordered_quantity > 0),

    CONSTRAINT nonnegative_unit_cost
        CHECK (unit_cost >= 0),

    CONSTRAINT unique_product_per_order
        UNIQUE (purchase_order_id, product_id)
);
