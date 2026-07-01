CREATE EXTENSION IF NOT EXISTS dblink;

INSERT INTO reviews (
    id,
    user_id,
    user_name,
    user_avatar_media_id,
    product_id,
    order_item_id,
    title,
    content_text,
    content_hash,
    rating,
    status,
    trust_score,
    sentiment,
    sentiment_confidence,
    sentiment_status,
    sentiment_analyzed_at,
    sentiment_error,
    created_at,
    updated_at
)
SELECT
    src.id,
    src.user_id,
    src.user_name,
    src.user_avatar_media_id,
    src.product_id,
    src.order_item_id,
    src.title,
    src.content_text,
    src.content_hash,
    src.rating,
    src.status::review_status,
    src.trust_score,
    NULL,
    NULL,
    'PENDING',
    NULL,
    NULL,
    src.created_at,
    src.updated_at
FROM dblink(
    'host=postgres dbname=furnisight_catalog_db user=postgres password=postgres port=5432',
    'SELECT id, user_id, user_name, user_avatar_media_id, product_id, order_item_id, title, content_text, content_hash, rating, status, trust_score, created_at, updated_at FROM reviews'
) AS src(
    id UUID,
    user_id UUID,
    user_name VARCHAR(255),
    user_avatar_media_id UUID,
    product_id UUID,
    order_item_id UUID,
    title VARCHAR(255),
    content_text TEXT,
    content_hash VARCHAR(255),
    rating INTEGER,
    status TEXT,
    trust_score NUMERIC(3,2),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
)
ON CONFLICT (id) DO NOTHING;
