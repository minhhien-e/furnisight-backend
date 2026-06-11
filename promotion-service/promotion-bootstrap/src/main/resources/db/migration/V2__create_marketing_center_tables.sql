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
    placements TEXT,
    original_amount DOUBLE PRECISION DEFAULT 0,
    final_amount DOUBLE PRECISION DEFAULT 0,
    saved_amount DOUBLE PRECISION DEFAULT 0,
    used_count BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS promotion_combo_items (
    id UUID PRIMARY KEY,
    combo_id UUID NOT NULL REFERENCES promotion_combos(id) ON DELETE CASCADE,
    product_id VARCHAR(80) NOT NULL,
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
