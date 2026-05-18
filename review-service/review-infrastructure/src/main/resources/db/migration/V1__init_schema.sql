CREATE TYPE review_status AS ENUM ('PENDING', 'VISIBLE', 'HIDDEN', 'SHADOW_BANNED', 'ARCHIVED');
CREATE TYPE vote_type AS ENUM ('UP', 'DOWN');

CREATE TABLE reviews (
                         id UUID PRIMARY KEY,
                         title VARCHAR(255) NOT NULL,
                         user_id UUID NOT NULL,
                         product_id UUID NOT NULL,
                         order_item_id UUID NOT NULL UNIQUE,

                         content_text TEXT NOT NULL,
                         content_hash VARCHAR(255) NOT NULL,

                         rating SMALLINT NOT NULL CHECK (rating >= 1 AND rating <= 5),
                         status review_status DEFAULT 'PENDING',
                         trust_score DECIMAL(3, 2) DEFAULT 0.50,

                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE review_votes (
                              user_id UUID NOT NULL,
                              review_id UUID NOT NULL REFERENCES reviews(id) ON DELETE CASCADE,
                              vote_type vote_type NOT NULL,
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              PRIMARY KEY (user_id, review_id)
);

CREATE INDEX idx_reviews_product_id ON reviews(product_id);
CREATE INDEX idx_reviews_status ON reviews(status);
ALTER TABLE reviews ALTER COLUMN rating TYPE INTEGER;
