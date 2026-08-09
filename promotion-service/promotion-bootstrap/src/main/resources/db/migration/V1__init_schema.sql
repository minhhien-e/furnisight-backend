-- ============================================================
-- V1__init_schema.sql
-- Initial schema for promotion-service
-- ============================================================

CREATE TABLE IF NOT EXISTS promotions (
    id UUID PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    icon VARCHAR(255),
    voucher_type VARCHAR(50) NOT NULL DEFAULT 'PUBLIC',
    discount_type VARCHAR(50) NOT NULL,
    discount_value DOUBLE PRECISION NOT NULL,
    max_discount DOUBLE PRECISION,
    min_order DOUBLE PRECISION,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_promotions_public_window
    ON promotions (voucher_type, active, end_date, code);

CREATE TABLE IF NOT EXISTS user_vouchers (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    promotion_id UUID NOT NULL REFERENCES promotions(id) ON DELETE CASCADE,
    is_used BOOLEAN DEFAULT FALSE,
    used_at TIMESTAMP,
    saved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_user_promotion UNIQUE (user_id, promotion_id)
);

CREATE INDEX IF NOT EXISTS idx_user_vouchers_user_unused
    ON user_vouchers (user_id, is_used, promotion_id);

CREATE TABLE IF NOT EXISTS marketing_campaigns (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    voucher_id UUID REFERENCES promotions(id) ON DELETE SET NULL,
    target_type VARCHAR(50) NOT NULL,
    target_user_ids TEXT,
    segment_key VARCHAR(80),
    channels TEXT,
    schedule_type VARCHAR(50) NOT NULL,
    scheduled_at TIMESTAMP,
    notification_title VARCHAR(255),
    notification_body TEXT,
    status VARCHAR(50) NOT NULL,
    sent_count BIGINT DEFAULT 0,
    active BOOLEAN DEFAULT TRUE,
    dispatched_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS marketing_notifications (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    body TEXT,
    target_type VARCHAR(50) NOT NULL,
    target_user_ids TEXT,
    segment_key VARCHAR(80),
    channels TEXT,
    send_type VARCHAR(50) NOT NULL,
    scheduled_at TIMESTAMP,
    related_voucher_id UUID REFERENCES promotions(id) ON DELETE SET NULL,
    status VARCHAR(50) NOT NULL,
    sent_count BIGINT DEFAULT 0,
    active BOOLEAN DEFAULT TRUE,
    dispatched_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS promotion_combos (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    discount_type VARCHAR(50) NOT NULL,
    discount_value DOUBLE PRECISION NOT NULL,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    image_media_id UUID,
    image_url TEXT,
    original_amount DOUBLE PRECISION DEFAULT 0,
    final_amount DOUBLE PRECISION DEFAULT 0,
    saved_amount DOUBLE PRECISION DEFAULT 0,
    used_count BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_promotion_combos_active_window
    ON promotion_combos (active, saved_amount DESC, used_count DESC, created_at DESC);

CREATE TABLE IF NOT EXISTS promotion_combo_items (
    id UUID PRIMARY KEY,
    combo_id UUID NOT NULL REFERENCES promotion_combos(id) ON DELETE CASCADE,
    product_id VARCHAR(80) NOT NULL,
    product_slug VARCHAR(255),
    variant_id VARCHAR(80),
    product_name VARCHAR(255),
    sku VARCHAR(120),
    category_name VARCHAR(255),
    image VARCHAR(255),
    price DOUBLE PRECISION DEFAULT 0,
    quantity INTEGER DEFAULT 1,
    snapshot_missing BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_promotion_combo_items_combo
    ON promotion_combo_items (combo_id);

CREATE TABLE IF NOT EXISTS marketing_dispatch_logs (
    id UUID PRIMARY KEY,
    source_type VARCHAR(50) NOT NULL,
    source_id UUID,
    user_id UUID,
    channel VARCHAR(50),
    status VARCHAR(50) NOT NULL,
    title VARCHAR(255),
    message TEXT,
    error TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
