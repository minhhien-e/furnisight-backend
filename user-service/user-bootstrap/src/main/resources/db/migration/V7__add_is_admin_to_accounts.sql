ALTER TABLE accounts ADD COLUMN is_admin BOOLEAN NOT NULL DEFAULT FALSE;

-- Tự động đánh dấu các account cũ đang giữ Role Admin thành is_admin = true để không bị mất quyền
UPDATE accounts SET is_admin = TRUE 
WHERE id IN (
    SELECT ar.account_id FROM account_roles ar 
    JOIN roles r ON ar.role_id = r.id 
    WHERE r.name IN ('ROLE_ADMIN', 'ROLE_SUPER_ADMIN', 'ROLE_MANAGER', 'ROLE_STAFF')
);
