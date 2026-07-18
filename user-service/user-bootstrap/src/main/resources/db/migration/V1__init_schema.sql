-- ============================================================
-- V1__init_schema.sql
-- Initial Identity schema for user-service
-- ============================================================

-- accounts
CREATE TABLE accounts
(
    id                    UUID        NOT NULL PRIMARY KEY,
    email                 VARCHAR(255) UNIQUE,
    password_hash         VARCHAR(255),
    status                VARCHAR(20) NOT NULL DEFAULT 'UNVERIFIED',
    failed_login_attempts INT         NOT NULL DEFAULT 0,
    lockout_end           TIMESTAMP,
    is_admin              BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at            TIMESTAMP,
    updated_at            TIMESTAMP
);

-- roles
CREATE TABLE roles
(
    id          UUID         NOT NULL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    permissions BIGINT       NOT NULL DEFAULT 0,
    position    INT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP
);

-- account_roles  (join table)
CREATE TABLE account_roles
(
    id         UUID NOT NULL PRIMARY KEY,
    account_id UUID NOT NULL REFERENCES accounts (id) ON DELETE CASCADE,
    role_id    UUID NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE (account_id, role_id)
);

-- account_tokens  (JWT refresh token pairs)
CREATE TABLE account_tokens
(
    id                       UUID         NOT NULL PRIMARY KEY,
    account_id               UUID         NOT NULL REFERENCES accounts (id) ON DELETE CASCADE,
    access_token             TEXT         NOT NULL,
    access_token_expiration  TIMESTAMP    NOT NULL,
    access_token_revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
    refresh_token            VARCHAR(512) NOT NULL UNIQUE,
    refresh_token_expiration TIMESTAMP    NOT NULL,
    refresh_token_revoked    BOOLEAN      NOT NULL DEFAULT FALSE,
    roles                    VARCHAR(255)[],
    created_at               TIMESTAMP,
    updated_at               TIMESTAMP
);

-- bans
CREATE TABLE bans
(
    id         UUID         NOT NULL PRIMARY KEY,
    account_id UUID         NOT NULL REFERENCES accounts (id) ON DELETE CASCADE,
    reason     VARCHAR(500) NOT NULL,
    expires_at TIMESTAMP,
    is_active  BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- social_accounts
CREATE TABLE social_accounts
(
    id               UUID        NOT NULL PRIMARY KEY,
    account_id       UUID        NOT NULL REFERENCES accounts (id) ON DELETE CASCADE,
    provider         VARCHAR(50) NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    email            VARCHAR(255),
    expires_at       TIMESTAMP,
    created_at       TIMESTAMP,
    updated_at       TIMESTAMP,
    UNIQUE (provider, provider_user_id)
);

-- outbox_messages
CREATE TABLE outbox_messages
(
    id             UUID PRIMARY KEY,
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id   VARCHAR(255) NOT NULL,
    type           VARCHAR(255) NOT NULL,
    payload        TEXT         NOT NULL,
    created_at     TIMESTAMP    NOT NULL,
    processed_at   TIMESTAMP NULL,
    error_message  TEXT NULL,
    retry_count    INT          NOT NULL DEFAULT 0,
    next_retry_at  TIMESTAMP NULL,
    failed         BOOLEAN      NOT NULL DEFAULT FALSE,
    updated_at     TIMESTAMP
);

-- indexes
CREATE INDEX idx_account_tokens_account_id ON account_tokens (account_id);
CREATE INDEX idx_bans_account_id ON bans (account_id);
CREATE INDEX idx_social_accounts_account_id ON social_accounts (account_id);
CREATE INDEX idx_account_roles_account_id ON account_roles (account_id);
CREATE INDEX idx_outbox_unprocessed ON outbox_messages (processed_at) WHERE processed_at IS NULL;
CREATE INDEX idx_outbox_retry ON outbox_messages (next_retry_at) WHERE failed = FALSE;

-- ============================================================
-- Profile schema for user-service
-- ============================================================

CREATE TABLE user_profiles
(
    id            UUID         NOT NULL PRIMARY KEY,
    account_id    UUID         NOT NULL UNIQUE REFERENCES accounts (id) ON DELETE CASCADE,
    display_name  VARCHAR(100),
    full_name     VARCHAR(200),
    avatar_media_id UUID,
    avatar_url    VARCHAR(2048),
    bio           VARCHAR(500),
    email         VARCHAR(255),
    date_of_birth DATE,
    gender        VARCHAR(10),
    created_at    TIMESTAMP,
    updated_at    TIMESTAMP
);

CREATE INDEX idx_user_profiles_account_id ON user_profiles (account_id);

-- ============================================================
-- Favorite product state
-- ============================================================

CREATE TABLE favorite_products
(
    id         UUID      NOT NULL PRIMARY KEY,
    account_id UUID      NOT NULL REFERENCES accounts (id) ON DELETE CASCADE,
    product_id UUID      NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_favorite_products_account_product UNIQUE (account_id, product_id)
);

CREATE INDEX idx_favorite_products_account_id ON favorite_products (account_id);
CREATE INDEX idx_favorite_products_product_id ON favorite_products (product_id);

-- ============================================================
-- User delivery addresses
-- ============================================================

CREATE TABLE IF NOT EXISTS user_addresses
(
    id            UUID         NOT NULL PRIMARY KEY,
    account_id    UUID         NOT NULL REFERENCES user_profiles (account_id) ON DELETE CASCADE,
    full_name     VARCHAR(100) NOT NULL,
    phone         VARCHAR(20)  NOT NULL,
    province_code VARCHAR(20)  NOT NULL,
    province_name VARCHAR(100) NOT NULL,
    ward_code     VARCHAR(20)  NOT NULL,
    ward_name     VARCHAR(100) NOT NULL,
    detail        VARCHAR(255) NOT NULL,
    type          VARCHAR(20)  NOT NULL,
    is_default    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
