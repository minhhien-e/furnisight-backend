package com.furnisight.review.core.repository;

import com.furnisight.review.core.model.entity.ReviewEditHistory;
import java.util.Optional;
import java.util.UUID;

public interface ReviewEditHistoryRepository {
    void save(ReviewEditHistory history);
}
