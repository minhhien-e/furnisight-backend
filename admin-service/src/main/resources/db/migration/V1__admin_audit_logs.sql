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
