CREATE TABLE authorization_codes (
    id VARCHAR(26) NOT NULL PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    client_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(26) NOT NULL,
    redirect_uri VARCHAR(1000) NOT NULL,
    code_challenge VARCHAR(255) UNIQUE NOT NULL,
    code_challenge_method VARCHAR(15) NOT NULL,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    expiration_date TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL
);