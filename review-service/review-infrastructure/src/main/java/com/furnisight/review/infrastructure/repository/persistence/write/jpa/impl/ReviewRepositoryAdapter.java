package com.furnisight.review.infrastructure.repository.persistence.write.jpa.impl;

import com.furnisight.review.core.model.entity.Review;

import com.furnisight.review.core.repository.ReviewWritePort;
import com.furnisight.review.infrastructure.repository.persistence.write.jpa.JpaReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReviewRepositoryAdapter implements ReviewWritePort {

    private final JpaReviewRepository jpaRepository;

    @Override
    public void save(Review review) {
        jpaRepository.save(review);
    }

    @Override
    public Optional<Review> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
