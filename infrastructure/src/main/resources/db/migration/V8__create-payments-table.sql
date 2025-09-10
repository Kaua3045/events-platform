CREATE TABLE payments (
    id VARCHAR(26) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    order_id VARCHAR(26) UNIQUE NOT NULL,
    transaction_id VARCHAR(32) NOT NULL,
    status VARCHAR(20) NOT NULL,
    method VARCHAR(30) NOT NULL,
    amount  NUMERIC(10, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    paid_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE payment_details (
    id VARCHAR(26) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    qr_code VARCHAR(255) NULL,
    qr_code_image_url VARCHAR(255) NULL,
    expires_in int NOT NULL,
    payment_token VARCHAR(100) NULL,
    installments int DEFAULT 0,
    user_id VARCHAR(26) NULL
)
