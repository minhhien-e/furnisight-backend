-- >>> Appended from V1__init_media_schema.sql <<<

-- ============================================================
-- V1__init_media_schema.sql
-- Single-table media schema using Cloudinary as storage backend
-- ============================================================

CREATE TABLE media_assets
(
    id                    UUID         NOT NULL PRIMARY KEY,
    cloudinary_public_id  VARCHAR(500) NOT NULL UNIQUE,
    url                   VARCHAR(2048) NOT NULL,
    secure_url            VARCHAR(2048) NOT NULL,
    owner_id              UUID         NOT NULL,
    owner_type            VARCHAR(50)  NOT NULL,
    media_type            VARCHAR(20)  NOT NULL,
    state                 VARCHAR(20)  NOT NULL,
    original_filename     VARCHAR(500),
    mime_type             VARCHAR(100),
    size_bytes            BIGINT,
    format                VARCHAR(20),
    width                 INT,
    height                INT,
    created_at            TIMESTAMP    NOT NULL,
    updated_at            TIMESTAMP    NOT NULL
);

CREATE INDEX idx_media_assets_owner ON media_assets (owner_id, owner_type);

-- >>> Appended from V2__allow_pending_direct_upload.sql <<<

ALTER TABLE media_assets
    ALTER COLUMN cloudinary_public_id DROP NOT NULL,
    ALTER COLUMN url DROP NOT NULL,
    ALTER COLUMN secure_url DROP NOT NULL;
