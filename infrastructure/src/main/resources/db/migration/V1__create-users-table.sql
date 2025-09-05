CREATE TABLE users (
    id VARCHAR(26) NOT NULL PRIMARY KEY,
    first_name VARCHAR(110) NOT NULL,
    last_name VARCHAR(110) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    document_number VARCHAR(18) NULL,
    document_type VARCHAR(5) NULL,
    phone_e164 VARCHAR(20) NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL
);