package com.furnisight.review.infrastructure.repository.persistence.write.jpa;

import com.furnisight.review.core.model.entity.ReviewVote;
import com.furnisight.review.core.model.valueobject.ReviewVoteId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataReviewVoteRepository extends JpaRepository<ReviewVote, UUID> {

    Optional<ReviewVote> findById(ReviewVoteId id);
}
