package com.furnisight.review.infrastructure.database.repository.jpa;

import com.furnisight.review.core.model.entity.ReviewVote;
import com.furnisight.review.core.model.valueobject.ReviewVoteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewVoteJpaRepository extends JpaRepository<ReviewVote, ReviewVoteId> {
}
