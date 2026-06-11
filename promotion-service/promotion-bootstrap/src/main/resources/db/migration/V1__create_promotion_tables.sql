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

CREATE TABLE IF NOT EXISTS user_vouchers (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    promotion_id UUID NOT NULL REFERENCES promotions(id) ON DELETE CASCADE,
    is_used BOOLEAN DEFAULT FALSE,
    used_at TIMESTAMP,
    saved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_user_promotion UNIQUE (user_id, promotion_id)
);
