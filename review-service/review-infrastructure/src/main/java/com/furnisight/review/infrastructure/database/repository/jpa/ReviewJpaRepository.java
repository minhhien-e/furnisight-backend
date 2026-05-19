package com.furnisight.review.infrastructure.database.repository.jpa;

import com.furnisight.review.core.model.entity.Review;
import com.furnisight.review.core.model.enums.ReviewStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewJpaRepository extends JpaRepository<Review, UUID> {
    Optional<Review> findByOrderItemId(UUID orderItemId);

    List<Review> findByProductIdAndStatusInOrderByCreatedAtDesc(UUID productId, List<ReviewStatus> statuses, Pageable pageable);
}
