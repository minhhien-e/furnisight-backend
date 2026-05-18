package com.furnisight.review.infrastructure.database.repository.impl;

import com.furnisight.review.core.model.entity.Review;
import com.furnisight.review.core.repository.ReviewWritePort;
import com.furnisight.review.infrastructure.database.repository.jpa.ReviewJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewWritePort {

    private final ReviewJpaRepository jpaRepository;

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
