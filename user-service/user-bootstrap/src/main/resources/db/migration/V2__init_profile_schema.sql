-- ============================================================
-- V2__init_profile_schema.sql
-- Profile schema for user-service
-- ============================================================

CREATE TABLE user_profiles
(
    id            UUID         NOT NULL PRIMARY KEY,
    account_id    UUID         NOT NULL UNIQUE REFERENCES accounts (id) ON DELETE CASCADE,
    display_name  VARCHAR(100),
    first_name    VARCHAR(100),
    last_name     VARCHAR(100),
    avatar_media_id UUID,
    bio           VARCHAR(500),
    email         VARCHAR(255),
    date_of_birth DATE,
    gender        VARCHAR(10),
    created_at    TIMESTAMP,
    updated_at    TIMESTAMP
);

CREATE INDEX idx_user_profiles_account_id ON user_profiles (account_id);
