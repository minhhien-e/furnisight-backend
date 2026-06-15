ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS tracking_code VARCHAR(160);

CREATE TABLE IF NOT EXISTS order_status_history (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    order_code VARCHAR(50) NOT NULL,
    previous_status VARCHAR(50),
    next_status VARCHAR(50) NOT NULL,
    actor_id UUID,
    actor_type VARCHAR(40) NOT NULL,
    tracking_code VARCHAR(160),
    note TEXT,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_order_status_history_order_id
    ON order_status_history(order_id, created_at);
