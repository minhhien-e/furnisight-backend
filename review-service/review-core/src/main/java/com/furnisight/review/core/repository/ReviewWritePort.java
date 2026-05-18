package com.furnisight.review.core.repository;

import com.furnisight.review.core.model.entity.Review;
import java.util.Optional;
import java.util.UUID;

public interface ReviewWritePort {
    void save(Review review);

    Optional<Review> findById(UUID id);

    void deleteById(UUID id);
}
