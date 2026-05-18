package com.furnisight.review.core.repository;

import com.furnisight.review.core.model.entity.ReviewVote;
import java.util.Optional;
import java.util.UUID;

public interface ReviewVoteRepository {

    Optional<ReviewVote> findByReviewIdAndUserId(UUID reviewId, UUID userId);
    void save(ReviewVote vote);

    void delete(ReviewVote vote);
}
