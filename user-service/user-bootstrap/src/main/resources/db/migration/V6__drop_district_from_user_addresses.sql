ALTER TABLE user_addresses
    DROP COLUMN IF EXISTS district_code,
    DROP COLUMN IF EXISTS district_name;
