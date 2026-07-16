CREATE TABLE purchase_orders (
    id BIGSERIAL PRIMARY KEY,
    supplier_id BIGINT NOT NULL,
    destination_location_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    order_date TIMESTAMP,
    expected_delivery_date DATE,
    received_at TIMESTAMP,
    notes VARCHAR(500),

    CONSTRAINT fk_po_supplier
        FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
    CONSTRAINT fk_po_location
        FOREIGN KEY (destination_location_id) REFERENCES locations(id)
);
