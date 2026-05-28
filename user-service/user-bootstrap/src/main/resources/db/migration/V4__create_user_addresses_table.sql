CREATE TABLE IF NOT EXISTS user_addresses (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    province_code VARCHAR(20) NOT NULL,
    province_name VARCHAR(100) NOT NULL,
    district_code VARCHAR(20) NOT NULL,
    district_name VARCHAR(100) NOT NULL,
    ward_code VARCHAR(20) NOT NULL,
    ward_name VARCHAR(100) NOT NULL,
    detail VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE
);

ALTER TABLE user_addresses ADD CONSTRAINT fk_user_addresses_account_id FOREIGN KEY (account_id) REFERENCES user_profiles(account_id) ON DELETE CASCADE;
