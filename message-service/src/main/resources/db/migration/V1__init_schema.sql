-- ============================================================
-- V1__init_schema.sql
-- Message Service – initial schema
-- Generated from JPA entities to replace ddl-auto: update
-- ============================================================

CREATE TABLE IF NOT EXISTS conversations (
    id                   SERIAL PRIMARY KEY,
    buyer_id             INTEGER,
    staff_id             INTEGER,
    channel              VARCHAR(32)  NOT NULL DEFAULT 'SUPPORT',
    status               VARCHAR(32)  NOT NULL DEFAULT 'OPEN',
    priority             VARCHAR(32)  NOT NULL DEFAULT 'MEDIUM',
    assigned_admin_id    INTEGER,
    last_message_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    last_message_content VARCHAR(255) NOT NULL DEFAULT '',
    closed_at            TIMESTAMP,
    created_at           TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_conversations_channel  CHECK (channel  IN ('SUPPORT', 'SALES', 'TECHNICAL')),
    CONSTRAINT chk_conversations_status   CHECK (status   IN ('OPEN', 'ASSIGNED', 'IN_PROGRESS', 'WAITING_CUSTOMER', 'RESOLVED', 'CLOSED')),
    CONSTRAINT chk_conversations_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT'))
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_conversations_buyer_id         ON conversations (buyer_id) WHERE buyer_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_conversations_assigned_admin   ON conversations (assigned_admin_id);
CREATE INDEX IF NOT EXISTS idx_conversations_status           ON conversations (status);
CREATE INDEX IF NOT EXISTS idx_conversations_last_message_at  ON conversations (last_message_at DESC);

-- ============================================================

CREATE TABLE IF NOT EXISTS messages (
    id               SERIAL PRIMARY KEY,
    conversation_id  INTEGER      NOT NULL REFERENCES conversations (id) ON DELETE CASCADE,
    sender_id        INTEGER      NOT NULL,
    receiver_id      INTEGER      NOT NULL,
    content          VARCHAR(2000) NOT NULL,
    type             VARCHAR(32)  NOT NULL DEFAULT 'TEXT',
    file_id          INTEGER,
    media_id         VARCHAR(255),
    attachment_url   VARCHAR(2000),
    attachment_name  VARCHAR(255),
    attachment_type  VARCHAR(120),
    attachment_size  BIGINT,
    attachments      TEXT,
    is_internal      BOOLEAN      NOT NULL DEFAULT FALSE,
    is_read          BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_messages_type CHECK (type IN ('TEXT', 'IMAGE', 'FILE', 'ORDER_LINK', 'PRODUCT_LINK'))
);

CREATE INDEX IF NOT EXISTS idx_messages_conversation_id ON messages (conversation_id);
CREATE INDEX IF NOT EXISTS idx_messages_sender_id       ON messages (sender_id);
CREATE INDEX IF NOT EXISTS idx_messages_created_at      ON messages (created_at DESC);

-- ============================================================

CREATE TABLE IF NOT EXISTS message_templates (
    id         SERIAL PRIMARY KEY,
    title      VARCHAR(255) NOT NULL,
    content    TEXT         NOT NULL,
    category   VARCHAR(50),
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_message_templates_category ON message_templates (category);
CREATE INDEX IF NOT EXISTS idx_message_templates_active   ON message_templates (active);
