package com.furnisight.review.infrastructure.database.repository.jpa;

import com.furnisight.review.core.model.entity.ReviewEditHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewEditHistoryJpaRepository extends JpaRepository<ReviewEditHistory, UUID> {
    Optional<ReviewEditHistory> findFirstByReviewIdOrderByEditedAtDesc(UUID reviewId);
}
