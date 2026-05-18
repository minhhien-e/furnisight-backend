-- 1. Dọn dẹp và tạo lại Type Enum cho chuẩn
DO $$
    BEGIN
        -- Xóa type cũ (UP/DOWN) nếu tồn tại để tránh xung đột
        IF EXISTS (SELECT 1 FROM pg_type WHERE typname = 'vote_type') THEN
            DROP TYPE vote_type CASCADE;
        END IF;

        -- Tạo type mới khớp 100% với Java Enum
        CREATE TYPE vote_type AS ENUM ('LIKE', 'DISLIKE', 'NONE');
    END $$;

-- 2. Tạo bảng review_votes
CREATE TABLE IF NOT EXISTS review_votes (
                                            user_id UUID NOT NULL,
                                            review_id UUID NOT NULL,
                                            vote_type vote_type NOT NULL,
                                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Primary Key phức hợp cho @EmbeddedId
                                            CONSTRAINT pk_review_votes PRIMARY KEY (user_id, review_id),

    -- Khóa ngoại trỏ sang bảng reviews
                                            CONSTRAINT fk_review_votes_review FOREIGN KEY (review_id)
                                                REFERENCES reviews(id) ON DELETE CASCADE
);

-- 3. Index để đếm Like/Dislike nhanh hơn
CREATE INDEX IF NOT EXISTS idx_review_votes_review_id ON review_votes(review_id);
