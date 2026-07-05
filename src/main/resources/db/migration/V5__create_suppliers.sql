CREATE TABLE suppliers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    contact_name VARCHAR(100),
    email VARCHAR(150),
    phone VARCHAR(30),
    address TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE
);