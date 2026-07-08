CREATE TABLE locations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    type VARCHAR(50) NOT NULL,
    address VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(2),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_location_type CHECK (
        type IN (
            'WAREHOUSE',
            'STORE',
            'SERVICE_CENTER',
            'RETURNS_CENTER',
            'OVERFLOW_STORAGE',
            'DAMAGED_GOODS'
        )
    )
);
