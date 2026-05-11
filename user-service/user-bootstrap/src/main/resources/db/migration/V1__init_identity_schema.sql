-- ============================================================
-- V1__init_schema.sql
-- Initial Identity schema for user-service
-- ============================================================

-- accounts
CREATE TABLE accounts
(
    id                    UUID        NOT NULL PRIMARY KEY,
    username              VARCHAR(100) UNIQUE,
    email                 VARCHAR(255) UNIQUE,
    password_hash         VARCHAR(255),
    status                VARCHAR(20) NOT NULL DEFAULT 'UNVERIFIED',
    failed_login_attempts INT         NOT NULL DEFAULT 0,
    lockout_end           TIMESTAMP,
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
    created_at               TIMESTAMP,
    updated_at               TIMESTAMP
);

-- verification_requests (unified table for all OTP-based flows:
--   ACCOUNT_VERIFICATION, PASSWORD_RESET, EMAIL_CHANGE, PHONE_CHANGE)
CREATE TABLE verification_requests
(
    id              UUID        NOT NULL PRIMARY KEY,
    account_id      UUID        NOT NULL REFERENCES accounts (id) ON DELETE CASCADE,
    type            VARCHAR(30) NOT NULL,  -- VerificationType enum
    step            VARCHAR(20) NOT NULL DEFAULT 'STEP_1_PENDING', -- VerificationStep enum
    target_contact  VARCHAR(255),          -- current email/phone (step 1 destination)
    new_contact     VARCHAR(255),          -- new email/phone (step 2, contact-change flows)
    otp_code        VARCHAR(100),          -- OTP or token value
    otp_expires_at  TIMESTAMP,             -- OTP-level TTL
    expires_at      TIMESTAMP   NOT NULL,  -- overall session TTL
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP
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




-- outbound_messages
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
CREATE INDEX idx_verification_requests_account_type ON verification_requests (account_id, type);
CREATE INDEX idx_verification_requests_otp ON verification_requests (otp_code, type);
CREATE INDEX idx_bans_account_id ON bans (account_id);
CREATE INDEX idx_social_accounts_account_id ON social_accounts (account_id);
CREATE INDEX idx_account_roles_account_id ON account_roles (account_id);
CREATE INDEX idx_outbox_unprocessed
    ON outbox_messages (processed_at) WHERE processed_at IS NULL;

CREATE INDEX idx_outbox_retry
    ON outbox_messages (next_retry_at) WHERE failed = FALSE;
