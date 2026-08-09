-- ============================================================
-- V1__init_order_schema.sql
-- Initial schema for order-service
-- ============================================================

CREATE TABLE orders (
    id UUID PRIMARY KEY,
    order_code VARCHAR(50) UNIQUE NOT NULL,
    user_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    sub_total DOUBLE PRECISION,
    total_amount DOUBLE PRECISION,
    shipping_fee DOUBLE PRECISION,
    shipping_discount DOUBLE PRECISION,
    discount_amount DOUBLE PRECISION,
    insurance_fee DOUBLE PRECISION,
    saved_amount DOUBLE PRECISION,
    shipping_address_name VARCHAR(255),
    shipping_address_phone VARCHAR(50),
    shipping_address_detail TEXT,
    shipping_method VARCHAR(100),
    customer_note TEXT,
    payment_method VARCHAR(50),
    payment_status VARCHAR(50),
    paid_amount DOUBLE PRECISION,
    paid_at TIMESTAMP,
    order_created_at TIMESTAMP,
    payment_initiated_at TIMESTAMP,
    payment_completed_at TIMESTAMP,
    payment_failed_at TIMESTAMP,
    shop_voucher_code VARCHAR(255),
    shipping_voucher_code VARCHAR(255),
    combo_id VARCHAR(255),
    combo_discount DOUBLE PRECISION,
    tracking_code VARCHAR(160),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE order_items (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    product_id VARCHAR(50) NOT NULL,
    slug VARCHAR(255),
    variant_id VARCHAR(50),
    category_name VARCHAR(100),
    product_name VARCHAR(255) NOT NULL,
    color VARCHAR(100),
    material VARCHAR(100),
    warranty VARCHAR(100),
    weight DOUBLE PRECISION,
    length DOUBLE PRECISION,
    width DOUBLE PRECISION,
    height DOUBLE PRECISION,
    price DOUBLE PRECISION NOT NULL,
    quantity INTEGER NOT NULL,
    image_url TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS stock_reservations (
    id UUID PRIMARY KEY,
    order_code VARCHAR(50) NOT NULL,
    product_id UUID NOT NULL,
    product_variant_id UUID,
    quantity INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_stock_res_order_code FOREIGN KEY (order_code) REFERENCES orders(order_code)
);

CREATE INDEX idx_stock_res_order_code ON stock_reservations(order_code);

CREATE TABLE IF NOT EXISTS outbox_messages (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    processed_at TIMESTAMP,
    error_message TEXT,
    retry_count INT NOT NULL DEFAULT 0,
    next_retry_at TIMESTAMP,
    failed BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_outbox_messages_unprocessed ON outbox_messages (processed_at) WHERE processed_at IS NULL AND failed = false;
CREATE INDEX idx_outbox_messages_retry ON outbox_messages (next_retry_at) WHERE next_retry_at IS NOT NULL AND failed = false;

CREATE TABLE IF NOT EXISTS order_status_history (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    order_code VARCHAR(50) NOT NULL,
    previous_status VARCHAR(50),
    next_status VARCHAR(50) NOT NULL,
    actor_id UUID,
    actor_type VARCHAR(40) NOT NULL,
    tracking_code VARCHAR(160),
    note TEXT,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_order_status_history_order_id ON order_status_history(order_id, created_at);
