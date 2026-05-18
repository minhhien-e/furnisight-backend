CREATE TABLE review_processing_job (
    -- Các trường từ BaseEntity
                                       id UUID PRIMARY KEY,
                                       created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       version BIGINT DEFAULT 0,

    -- Các trường của ReviewProcessingJob
                                       review_id UUID NOT NULL,
                                       job_type VARCHAR(50) NOT NULL, -- Enum map sang String
                                       status VARCHAR(50) NOT NULL,   -- Enum map sang String
                                       moderation_reason VARCHAR(100), -- Nullable

                                       triggered_by VARCHAR(50) NOT NULL,
                                       trigger_actor_id VARCHAR(255) NOT NULL,
                                       processed_by_node_id VARCHAR(255),

                                       payload TEXT,
                                       retry_count INTEGER NOT NULL DEFAULT 0,
                                       error_message TEXT,

    -- Constraint (Tùy chọn: nếu muốn xóa review thì xóa luôn job liên quan)
                                       CONSTRAINT fk_job_review FOREIGN KEY (review_id) REFERENCES reviews(id) ON DELETE CASCADE
);

-- Index để worker quét job nhanh hơn
CREATE INDEX idx_review_job_status ON review_processing_job(status);
CREATE INDEX idx_review_job_review_id ON review_processing_job(review_id);
