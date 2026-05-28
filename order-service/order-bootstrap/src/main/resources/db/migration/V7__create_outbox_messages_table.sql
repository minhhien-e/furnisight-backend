CREATE TABLE outbox_messages (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    processed_at TIMESTAMP,
    error_message TEXT,
    retry_count INT NOT NULL DEFAULT 0,
    next_retry_at TIMESTAMP,
    failed BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_outbox_messages_unprocessed ON outbox_messages (processed_at) WHERE processed_at IS NULL AND failed = false;
CREATE INDEX idx_outbox_messages_retry ON outbox_messages (next_retry_at) WHERE next_retry_at IS NOT NULL AND failed = false;
