-- >>> Appended from V1__admin_audit_logs.sql <<<

CREATE TABLE IF NOT EXISTS admin_audit_logs (
    id UUID PRIMARY KEY,
    actor_id UUID,
    action_type VARCHAR(40) NOT NULL,
    action VARCHAR(160) NOT NULL,
    resource_type VARCHAR(80) NOT NULL,
    resource_id VARCHAR(160),
    result VARCHAR(40) NOT NULL,
    detail TEXT,
    ip_address VARCHAR(80),
    user_agent TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_admin_audit_logs_created_at ON admin_audit_logs(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_admin_audit_logs_actor_id ON admin_audit_logs(actor_id);
CREATE INDEX IF NOT EXISTS idx_admin_audit_logs_action_type ON admin_audit_logs(action_type);
CREATE INDEX IF NOT EXISTS idx_admin_audit_logs_result ON admin_audit_logs(result);
CREATE INDEX IF NOT EXISTS idx_admin_audit_logs_resource ON admin_audit_logs(resource_type, resource_id);

-- >>> Appended from V2__revenue_snapshots.sql <<<

CREATE TABLE IF NOT EXISTS revenue_snapshots (
    id UUID PRIMARY KEY,
    year_month VARCHAR(7) NOT NULL UNIQUE,
    total_revenue DOUBLE PRECISION NOT NULL DEFAULT 0,
    order_count BIGINT NOT NULL DEFAULT 0,
    mom_change_pct DOUBLE PRECISION,
    label VARCHAR(20) NOT NULL,
    snapshot_at TIMESTAMP NOT NULL
);

-- >>> Appended from V3__add_actor_info_to_audit_logs.sql <<<

ALTER TABLE admin_audit_logs
ADD COLUMN actor_name VARCHAR(255);

-- >>> Appended from V4__fix_revenue_snapshots_schema.sql <<<

ALTER TABLE revenue_snapshots ALTER COLUMN year_month TYPE VARCHAR(20);
ALTER TABLE revenue_snapshots DROP COLUMN IF EXISTS label;
