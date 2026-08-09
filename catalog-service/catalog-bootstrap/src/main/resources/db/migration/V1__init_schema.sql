-- ============================================================
-- V1__init_schema.sql
-- Initial schema for catalog-service (Consolidated)
-- ============================================================

-- Enable the pg_trgm extension
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE IF NOT EXISTS room_types (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(255) UNIQUE,
    description TEXT,
    image_url TEXT,
    media_id UUID,
    visible BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS categories (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(255) UNIQUE,
    parent_id UUID REFERENCES categories(id) ON DELETE SET NULL,
    room_type_id UUID REFERENCES room_types(id) ON DELETE SET NULL,
    path VARCHAR(255),
    product_count INTEGER DEFAULT 0,
    icon_url TEXT,
    visible BOOLEAN NOT NULL DEFAULT TRUE,
    description TEXT,
    image_url TEXT,
    spec_template JSONB,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_categories_room_type_id ON categories(room_type_id);

CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY,
    category_id UUID REFERENCES categories(id) ON DELETE SET NULL,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE,
    sku VARCHAR(255) UNIQUE,
    description TEXT,
    product_status VARCHAR(50) NOT NULL,
    base_price NUMERIC(19, 2),
    features JSONB,
    specifications JSONB,
    image_url TEXT,
    image_media_id UUID,
    weight DOUBLE PRECISION,
    length DOUBLE PRECISION,
    width DOUBLE PRECISION,
    height DOUBLE PRECISION,
    color VARCHAR(100),
    sold_count INTEGER NOT NULL DEFAULT 0,
    rating DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    rating_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_products_category_id ON products(category_id);
CREATE INDEX IF NOT EXISTS idx_products_sku ON products(sku);
CREATE INDEX IF NOT EXISTS idx_products_status ON products(product_status);
CREATE INDEX IF NOT EXISTS idx_products_created_at ON products(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_products_sold_count ON products(sold_count DESC);
CREATE INDEX IF NOT EXISTS idx_products_rating ON products(rating DESC);
CREATE INDEX IF NOT EXISTS idx_products_name_trgm ON products USING gin (LOWER(name) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_products_desc_trgm ON products USING gin (LOWER(description) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_products_slug_trgm ON products USING gin (LOWER(slug) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_products_specifications ON products USING gin (specifications);


CREATE TABLE IF NOT EXISTS product_variants (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    sku VARCHAR(100) NOT NULL UNIQUE CHECK (TRIM(sku) <> ''),
    price NUMERIC(19, 2),
    stock_quantity INTEGER NOT NULL DEFAULT 0,
    low_stock_threshold INTEGER NOT NULL DEFAULT 5 CHECK (low_stock_threshold BETWEEN 1 AND 9999),
    weight DOUBLE PRECISION,
    length DOUBLE PRECISION,
    width DOUBLE PRECISION,
    height DOUBLE PRECISION,
    material VARCHAR(255) NOT NULL,
    warranty VARCHAR(255),
    color VARCHAR(100),
    model_media_id UUID,
    model_url VARCHAR,
    supports_3d BOOLEAN NOT NULL DEFAULT FALSE,
    specifications JSONB,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_product_variants_product_id ON product_variants(product_id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_product_variants_sku ON product_variants (UPPER(sku));
CREATE INDEX IF NOT EXISTS idx_product_variants_specifications ON product_variants USING gin (specifications);

CREATE TABLE IF NOT EXISTS product_variant_images (
    id UUID PRIMARY KEY,
    variant_id UUID NOT NULL REFERENCES product_variants(id) ON DELETE CASCADE,
    media_id UUID,
    image_url TEXT,
    position INTEGER DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_product_variant_images_variant_id ON product_variant_images(variant_id);

CREATE TABLE IF NOT EXISTS product_favorite_logs (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS outbox_messages (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITHOUT TIME ZONE,
    error_message TEXT,
    retry_count INTEGER NOT NULL DEFAULT 0,
    next_retry_at TIMESTAMP WITHOUT TIME ZONE,
    failed BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);
