CREATE TABLE events(
    id VARCHAR(26) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    organization_id VARCHAR(26) NOT NULL,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    type VARCHAR(20) NOT NULL,
    image_url TEXT,
    category_id VARCHAR(26) NOT NULL,
    start_at TIMESTAMP WITH TIME ZONE NOT NULL,
    finish_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE,

    address_street VARCHAR(255),
    address_number VARCHAR(50),
    address_complement VARCHAR(100),
    address_neighborhood VARCHAR(100),
    address_city VARCHAR(100),
    address_state VARCHAR(60),
    address_postal_code VARCHAR(20),
    address_country VARCHAR(10)
);

CREATE INDEX idx_unique_title_organization_id_events ON events(title, organization_id);