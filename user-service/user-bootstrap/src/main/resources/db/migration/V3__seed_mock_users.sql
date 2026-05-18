-- ============================================================
-- V3__seed_mock_users.sql
-- Seed default mock users and profiles for development
-- ============================================================

-- 1. Seed Roles
INSERT INTO roles (id, name, permissions, position, created_at, updated_at)
VALUES ('9b6eca13-0b92-466a-bbf5-141e6676edab', 'ADMIN', 15, 0, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- 2. Seed Accounts
-- Password for admin: admin (or whatever was originally hashed)
INSERT INTO accounts (id, username, email, password_hash, status, failed_login_attempts, created_at, updated_at)
VALUES ('f85b5fd8-d60e-4c7e-87ae-5912796d668e', 'admin', 'admin@furnisight.com', '$2a$10$3vmJClllx85o7OniTZqzze7l4Jcnv27BADtOGn5OqlFls73qQw5U2', 'ACTIVE', 0, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Password for minhhien7840@gmail.com
INSERT INTO accounts (id, username, email, password_hash, status, failed_login_attempts, created_at, updated_at)
VALUES ('52379d96-5238-4fd9-8383-bae82736bb3b', 'minhhien7840@gmail.com', 'minhhien7840@gmail.com', '$2a$10$lY9N2oCzyNpfbXjVk6D.7OsK.PkL8rwc0S0GHg3xj1NUzC./8iD06', 'ACTIVE', 0, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Password for 22130080@st.hcmuaf.edu.vn
INSERT INTO accounts (id, username, email, password_hash, status, failed_login_attempts, created_at, updated_at)
VALUES ('4b33e5c1-cae1-458d-b4b1-e568ddd766f6', '22130080@st.hcmuaf.edu.vn', '22130080@st.hcmuaf.edu.vn', '$2a$10$/tE2UveZaZ95LbJyzrtF4uykBfsY1pSewVCDxVhrTM3E4EQ8ce/nK', 'ACTIVE', 0, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- 3. Seed Account Roles (Link admin account to ADMIN role)
INSERT INTO account_roles (id, account_id, role_id, created_at, updated_at)
VALUES ('0a42e6d1-0a5d-496f-98e0-88fb16717450', 'f85b5fd8-d60e-4c7e-87ae-5912796d668e', '9b6eca13-0b92-466a-bbf5-141e6676edab', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- 4. Seed User Profiles
INSERT INTO user_profiles (id, account_id, display_name, first_name, last_name, avatar_url, email, phone_number, date_of_birth, gender, created_at, updated_at)
VALUES ('2a8cd0ca-1b8f-40d2-881b-1c315e0e66a1', '52379d96-5238-4fd9-8383-bae82736bb3b', 'Minh Hiển', 'Minh Hiển', 'Huỳnh', NULL, 'minhhien7840@gmail.com', NULL, NULL, NULL, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_profiles (id, account_id, display_name, first_name, last_name, avatar_url, email, phone_number, date_of_birth, gender, created_at, updated_at)
VALUES ('b8d737cf-1a6b-4fda-97ba-0da8d4bd9df5', '4b33e5c1-cae1-458d-b4b1-e568ddd766f6', 'Hiển', 'Hiển', 'Huỳnh Minh', NULL, '22130080@st.hcmuaf.edu.vn', NULL, NULL, NULL, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_profiles (id, account_id, display_name, first_name, last_name, avatar_url, email, phone_number, date_of_birth, gender, created_at, updated_at)
VALUES ('7bdf15ee-3982-4f24-a1db-397cfb49e1e0', 'f85b5fd8-d60e-4c7e-87ae-5912796d668e', 'Admin', 'Admin', 'FurniSight', NULL, 'admin@furnisight.com', NULL, NULL, NULL, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;
