CREATE TABLE tickets (
    id VARCHAR(26) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    event_id VARCHAR(26) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    price NUMERIC(10, 2) NOT NULL,
    quantity INT NOT NULL,
    sold INT NOT NULL DEFAULT 0,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE
);

ALTER TABLE tickets ADD CONSTRAINT chk_sold_quantity CHECK (sold <= quantity AND sold >= 0);