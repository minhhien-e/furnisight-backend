CREATE TABLE IF NOT EXISTS revenue_snapshots (
    id UUID PRIMARY KEY,
    year_month VARCHAR(7) NOT NULL UNIQUE,
    total_revenue DOUBLE PRECISION NOT NULL DEFAULT 0,
    order_count BIGINT NOT NULL DEFAULT 0,
    mom_change_pct DOUBLE PRECISION,
    label VARCHAR(20) NOT NULL,
    snapshot_at TIMESTAMP NOT NULL
);
