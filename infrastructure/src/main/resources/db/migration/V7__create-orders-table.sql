CREATE TABLE orders (
    id VARCHAR(26) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    user_id VARCHAR(26) NOT NULL,
    total_amount NUMERIC(10, 2) NOT NULL,
    payment_id VARCHAR(26) NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    failed_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE order_items (
    id VARCHAR(26) PRIMARY KEY,
    order_id VARCHAR(26) NOT NULL,
    event_id VARCHAR(26) NOT NULL,
    ticket_id VARCHAR(26) NOT NULL,
    quantity int NOT NULL,
    unit_price NUMERIC(10, 2) NOT NULL,
    total_price NUMERIC(10, 2) NOT NULL
);