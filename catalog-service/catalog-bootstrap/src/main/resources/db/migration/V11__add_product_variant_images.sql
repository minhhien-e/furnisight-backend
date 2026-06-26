CREATE TABLE IF NOT EXISTS product_variant_images (
    id UUID PRIMARY KEY,
    variant_id UUID NOT NULL REFERENCES product_variants(id) ON DELETE CASCADE,
    image_url TEXT NOT NULL,
    position INTEGER DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_product_variant_images_variant_id
    ON product_variant_images(variant_id);
