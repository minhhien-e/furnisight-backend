package com.furnisight.review.adapter.out.repository;

import com.furnisight.review.adapter.out.repository.ReviewJpaRepository;
import com.furnisight.review.application.review.port.out.repository.ReviewWritePort;
import com.furnisight.review.domain.entities.Review;
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
    public Optional<Review> findByOrderItemId(UUID orderItemId) {
        return jpaRepository.findByOrderItemId(orderItemId);
    }

    @Override
    public java.util.List<Review> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public java.util.List<Review> findBySentimentStatusIn(java.util.List<String> statuses) {
        return jpaRepository.findBySentimentStatusInOrderByCreatedAtAsc(statuses);
    }

    @Override
    public java.util.List<Review> findHeuristicNeutralReviews() {
        return jpaRepository.findHeuristicNeutralReviews();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}

