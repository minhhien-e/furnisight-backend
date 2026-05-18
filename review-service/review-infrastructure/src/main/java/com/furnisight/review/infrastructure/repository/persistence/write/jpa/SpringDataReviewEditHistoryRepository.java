package com.furnisight.review.infrastructure.repository.persistence.write.jpa;


import com.furnisight.review.core.model.entity.ReviewEditHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataReviewEditHistoryRepository extends JpaRepository<ReviewEditHistory, UUID> {
    Optional<ReviewEditHistory> findFirstByReviewIdOrderByEditedAtDesc(UUID reviewId);
}
