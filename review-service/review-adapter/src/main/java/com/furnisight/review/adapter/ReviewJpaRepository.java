package com.furnisight.review.adapter.database.repository.jpa;

import com.furnisight.review.domain.entities.Review;
import com.furnisight.review.domain.enums.ReviewStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewJpaRepository extends JpaRepository<Review, UUID> {
    Optional<Review> findByOrderItemId(UUID orderItemId);
    List<Review> findByProductIdAndStatusInOrderByCreatedAtDesc(UUID productId, List<ReviewStatus> statuses, Pageable pageable);
    List<Review> findBySentimentStatusInOrderByCreatedAtAsc(List<String> statuses);
    @Query(value = """
        SELECT *
        FROM reviews
        WHERE sentiment_status = 'COMPLETED'
          AND sentiment = 'NEUTRAL'
          AND sentiment_confidence = 0.6000
        ORDER BY created_at ASC
        """, nativeQuery = true)
    List<Review> findHeuristicNeutralReviews();

    @Query(value = "SELECT * FROM reviews WHERE rating >= 4 AND status::text = 'VISIBLE' ORDER BY rating DESC, RANDOM() LIMIT :limit", nativeQuery = true)
    List<Review> findTopRandomReviews(@Param("limit") int limit);
    
    List<Review> findByUserId(UUID userId);
}

