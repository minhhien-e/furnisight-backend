package com.furnisight.review.infrastructure.database.repository.jpa;

import com.furnisight.review.core.model.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewJpaRepository extends JpaRepository<Review, UUID> {
    Optional<Review> findByOrderItemId(UUID orderItemId);
}
