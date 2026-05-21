package com.furnisight.catalog.application.review.port.out.repository;

import com.furnisight.catalog.domain.entities.Review;
import java.util.Optional;
import java.util.UUID;

public interface ReviewWritePort {
    void save(Review review);
    Optional<Review> findById(UUID id);
    void deleteById(UUID id);
}
