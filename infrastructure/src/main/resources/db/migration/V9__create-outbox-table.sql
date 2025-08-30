CREATE TABLE outbox (
    id VARCHAR(26) PRIMARY KEY NOT NULL,
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id VARCHAR(26) NOT NULL,
    aggregate_version BIGINT NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    occurred_on TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(50) NOT NULL,
    last_attempt_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_outbox_status_and_occurred_on ON outbox (status, occurred_on);