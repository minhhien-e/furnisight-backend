CREATE TABLE media_asset
(
    id                UUID         NOT NULL,
    created_at        TIMESTAMP    NOT NULL,
    updated_at        TIMESTAMP    NOT NULL,
    version           BIGINT NULL,
    owner_id          UUID         NOT NULL,
    owner_type        VARCHAR(255) NOT NULL,
    media_type        VARCHAR(255) NOT NULL,
    state             VARCHAR(255) NOT NULL,
    original_key      VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NULL,
    mime_type         VARCHAR(100) NULL,
    size_bytes        BIGINT NULL,
    reject_reason     VARCHAR(500) NULL,
    CONSTRAINT pk_mediaasset PRIMARY KEY (id)
);

CREATE TABLE media_processing_job
(
    id            UUID         NOT NULL,
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP    NOT NULL,
    version       BIGINT NULL,
    asset_id      UUID         NOT NULL,
    job_type      VARCHAR(255) NOT NULL,
    status        VARCHAR(255) NOT NULL,
    retry_count   INT          NOT NULL,
    error_message TEXT NULL,
    CONSTRAINT pk_mediaprocessingjob PRIMARY KEY (id)
);

CREATE TABLE media_variant
(
    id           UUID         NOT NULL,
    created_at   TIMESTAMP    NOT NULL,
    updated_at   TIMESTAMP    NOT NULL,
    version      BIGINT NULL,
    asset_id     UUID         NOT NULL,
    variant_type VARCHAR(255) NOT NULL,
    width        INT NULL,
    height       INT NULL,
    format       VARCHAR(20) NULL,
    storage_key  VARCHAR(255) NOT NULL,
    size_bytes   BIGINT NULL,
    CONSTRAINT pk_mediavariant PRIMARY KEY (id)
);

ALTER TABLE media_asset
    ADD CONSTRAINT uc_mediaasset_originalkey UNIQUE (original_key);

ALTER TABLE media_variant
    ADD CONSTRAINT uc_mediavariant_storagekey UNIQUE (storage_key);

CREATE INDEX idx_media_asset_owner ON media_asset (owner_id, owner_type);

ALTER TABLE media_processing_job
    ADD CONSTRAINT FK_MEDIAPROCESSINGJOB_ON_ASSET FOREIGN KEY (asset_id) REFERENCES media_asset (id);

ALTER TABLE media_variant
    ADD CONSTRAINT FK_MEDIAVARIANT_ON_ASSET FOREIGN KEY (asset_id) REFERENCES media_asset (id);
