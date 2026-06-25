ALTER TABLE product_variants
    ADD COLUMN IF NOT EXISTS model_media_id UUID,
    ADD COLUMN IF NOT EXISTS model_url VARCHAR,
    ADD COLUMN IF NOT EXISTS supports_3d BOOLEAN NOT NULL DEFAULT FALSE;

-- Migrate existing 3D models to variants
UPDATE product_variants pv
SET model_media_id = p.model_media_id,
    model_url = p.model_url,
    supports_3d = p.supports_3d
FROM products p
WHERE pv.product_id = p.id AND p.supports_3d = TRUE;

ALTER TABLE products
    DROP COLUMN IF EXISTS model_media_id,
    DROP COLUMN IF EXISTS model_url,
    DROP COLUMN IF EXISTS supports_3d;
