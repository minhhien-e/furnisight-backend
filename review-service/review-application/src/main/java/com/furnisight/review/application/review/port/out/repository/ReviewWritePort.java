package com.furnisight.review.application.review.port.out.repository;

import com.furnisight.review.domain.entities.Review;
import java.util.Optional;
import java.util.UUID;

public interface ReviewWritePort {
    void save(Review review);
    Optional<Review> findById(UUID id);
    Optional<Review> findByOrderItemId(UUID orderItemId);
    java.util.List<Review> findByUserId(UUID userId);
    java.util.List<Review> findBySentimentStatusIn(java.util.List<String> statuses);
    java.util.List<Review> findHeuristicNeutralReviews();
    void deleteById(UUID id);
}

