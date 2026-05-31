package com.furnisight.catalog.infrastructure.database.repository.jpa;

import com.furnisight.catalog.domain.entities.Review;
import com.furnisight.catalog.domain.enums.ReviewStatus;
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

    @Query(value = "SELECT * FROM reviews WHERE rating >= 4 AND status::text = 'VISIBLE' ORDER BY rating DESC, RANDOM() LIMIT :limit", nativeQuery = true)
    List<Review> findTopRandomReviews(@Param("limit") int limit);
    
    List<Review> findByUserId(UUID userId);
}
