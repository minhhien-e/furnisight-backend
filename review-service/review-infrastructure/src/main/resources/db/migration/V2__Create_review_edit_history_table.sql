-- Thêm IF NOT EXISTS để an toàn tuyệt đối
CREATE TABLE IF NOT EXISTS review_edit_history (
                                                   id UUID PRIMARY KEY,
                                                   review_id UUID NOT NULL,
                                                   old_content_text TEXT,
                                                   old_content_hash VARCHAR(255),
                                                   new_content_text TEXT,
                                                   new_content_hash VARCHAR(255),
                                                   old_rating INTEGER,
                                                   new_rating INTEGER,
                                                   client_ip VARCHAR(45),
                                                   edited_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                                   CONSTRAINT fk_review_edit_history_review FOREIGN KEY (review_id) REFERENCES reviews(id) ON DELETE CASCADE
);

-- Index cũng cần phòng thủ tương tự
CREATE INDEX IF NOT EXISTS idx_review_edit_history_review_id ON review_edit_history(review_id);
CREATE INDEX IF NOT EXISTS idx_review_edit_history_edited_at ON review_edit_history(edited_at);
