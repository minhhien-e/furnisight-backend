package com.furnisight.review.infrastructure.repository.persistence.write.jpa.impl;

import com.furnisight.review.core.repository.ReviewVoteRepository;
import com.furnisight.review.core.model.entity.ReviewVote;
import com.furnisight.review.core.model.valueobject.ReviewVoteId;
import com.furnisight.review.infrastructure.repository.persistence.write.jpa.SpringDataReviewVoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ReviewVoteAdapter implements ReviewVoteRepository {

    private final SpringDataReviewVoteRepository jpaRepository;

    @Override
    public void save(ReviewVote vote) {
        jpaRepository.save(vote);
    }

    @Override
    public void delete(ReviewVote vote) {
        jpaRepository.delete(vote);
    }

    @Override
    public Optional<ReviewVote> findByReviewIdAndUserId(UUID reviewId, UUID userId) {
        ReviewVoteId id = new ReviewVoteId(reviewId, userId);
        return jpaRepository.findById(id);
    }
}
