BEGIN;

-- Minimal local seed for testing.
-- Login accounts:
--   admin@furnisight.store / Password123!
--   user@furnisight.store  / Password123!

INSERT INTO roles (id, name, permissions, position, created_at, updated_at)
VALUES
  ('11111111-1111-1111-1111-111111111111', 'USER', 0, 1, NOW(), NOW()),
  ('22222222-2222-2222-2222-222222222222', 'ADMIN', 31, 0, NOW(), NOW())
ON CONFLICT (name) DO UPDATE
SET
  permissions = EXCLUDED.permissions,
  position = EXCLUDED.position,
  updated_at = NOW();

INSERT INTO accounts (id, email, password_hash, status, failed_login_attempts, created_at, updated_at, is_admin)
VALUES
  (
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    'admin@furnisight.store',
    '$2a$04$t1MWsV/4axK/bHxX3pGfuOtkSI.CLwk0XiYwKuNoWYygduDZvQLOG',
    'ACTIVE',
    0,
    NOW(),
    NOW(),
    TRUE
  ),
  (
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    'user@furnisight.store',
    '$2a$04$t1MWsV/4axK/bHxX3pGfuOtkSI.CLwk0XiYwKuNoWYygduDZvQLOG',
    'ACTIVE',
    0,
    NOW(),
    NOW(),
    FALSE
  )
ON CONFLICT (email) DO UPDATE
SET
  password_hash = EXCLUDED.password_hash,
  status = EXCLUDED.status,
  failed_login_attempts = 0,
  is_admin = EXCLUDED.is_admin,
  updated_at = NOW();

INSERT INTO account_roles (id, account_id, role_id, created_at, updated_at)
SELECT
  'cccccccc-cccc-cccc-cccc-cccccccccccc',
  a.id,
  r.id,
  NOW(),
  NOW()
FROM accounts a
JOIN roles r ON r.name = 'ADMIN'
WHERE a.email = 'admin@furnisight.store'
ON CONFLICT (account_id, role_id) DO NOTHING;

INSERT INTO account_roles (id, account_id, role_id, created_at, updated_at)
SELECT
  'dddddddd-dddd-dddd-dddd-dddddddddddd',
  a.id,
  r.id,
  NOW(),
  NOW()
FROM accounts a
JOIN roles r ON r.name = 'USER'
WHERE a.email = 'user@furnisight.store'
ON CONFLICT (account_id, role_id) DO NOTHING;

INSERT INTO user_profiles (id, account_id, display_name, full_name, email, created_at, updated_at)
SELECT
  'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
  a.id,
  'Admin',
  'FurniSight Admin',
  a.email,
  NOW(),
  NOW()
FROM accounts a
WHERE a.email = 'admin@furnisight.store'
ON CONFLICT (account_id) DO UPDATE
SET
  display_name = EXCLUDED.display_name,
  full_name = EXCLUDED.full_name,
  email = EXCLUDED.email,
  updated_at = NOW();

INSERT INTO user_profiles (id, account_id, display_name, full_name, email, created_at, updated_at)
SELECT
  'ffffffff-ffff-ffff-ffff-ffffffffffff',
  a.id,
  'User',
  'FurniSight User',
  a.email,
  NOW(),
  NOW()
FROM accounts a
WHERE a.email = 'user@furnisight.store'
ON CONFLICT (account_id) DO UPDATE
SET
  display_name = EXCLUDED.display_name,
  full_name = EXCLUDED.full_name,
  email = EXCLUDED.email,
  updated_at = NOW();

INSERT INTO user_addresses (
  id,
  account_id,
  full_name,
  phone,
  province_code,
  province_name,
  ward_code,
  ward_name,
  detail,
  type,
  is_default,
  created_at,
  updated_at
)
SELECT
  '12121212-1212-1212-1212-121212121212'::uuid,
  a.id,
  'FurniSight Admin',
  '0901234567',
  '79',
  'TP HCM',
  '26824',
  'Thu Duc',
  '123 Admin Street',
  'HOME',
  TRUE,
  NOW(),
  NOW()
FROM accounts a
WHERE a.email = 'admin@furnisight.store'
UNION ALL
SELECT
  '34343434-3434-3434-3434-343434343434'::uuid,
  a.id,
  'FurniSight User',
  '0907654321',
  '79',
  'TP HCM',
  '26824',
  'Thu Duc',
  '456 User Street',
  'HOME',
  TRUE,
  NOW(),
  NOW()
FROM accounts a
WHERE a.email = 'user@furnisight.store'
ON CONFLICT (id) DO UPDATE
SET
  full_name = EXCLUDED.full_name,
  phone = EXCLUDED.phone,
  province_code = EXCLUDED.province_code,
  province_name = EXCLUDED.province_name,
  ward_code = EXCLUDED.ward_code,
  ward_name = EXCLUDED.ward_name,
  detail = EXCLUDED.detail,
  type = EXCLUDED.type,
  is_default = EXCLUDED.is_default,
  updated_at = NOW();

COMMIT;
