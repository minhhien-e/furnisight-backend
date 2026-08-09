CREATE TYPE review_status AS ENUM ('PENDING', 'VISIBLE', 'HIDDEN', 'SHADOW_BANNED', 'ARCHIVED');

CREATE TABLE IF NOT EXISTS reviews (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    user_name VARCHAR(255),
    user_avatar_media_id UUID,
    product_id UUID NOT NULL,
    order_item_id UUID NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    content_text TEXT NOT NULL,
    content_hash VARCHAR(255) NOT NULL,
    rating INTEGER NOT NULL,
    status review_status NOT NULL DEFAULT 'PENDING',
    trust_score NUMERIC(3, 2) DEFAULT 0.50,
    sentiment VARCHAR(32),
    sentiment_confidence NUMERIC(5, 4),
    sentiment_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    sentiment_analyzed_at TIMESTAMP WITHOUT TIME ZONE,
    sentiment_error TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_reviews_product_id ON reviews(product_id);
CREATE INDEX IF NOT EXISTS idx_reviews_sentiment_status ON reviews(sentiment_status);
