package com.furnisight.review.core.service.ReviewVote;

import com.furnisight.review.core.model.entity.ReviewVote;
import com.furnisight.review.core.model.enums.VoteType;
import com.furnisight.review.core.model.valueobject.ReviewVoteId;
import com.furnisight.review.core.repository.ReviewVoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewVoteService {

    private final ReviewVoteRepository voteRepository;

    @Transactional
    public void toggleVote(UUID reviewId, String userId, VoteType voteType) {
        ReviewVoteId voteId = new ReviewVoteId(
            userId,
            reviewId
        );
        Optional<ReviewVote> existingVote = voteRepository.findByReviewIdAndUserId(
            reviewId, userId
        );

        if (existingVote.isPresent()) {
            ReviewVote vote = existingVote.get();
            if (vote.getVoteType().equals(voteType)) {
                voteRepository.delete(vote);
            } else {
                vote.changeVote(voteType);
                voteRepository.save(vote);
            }
        } else {
            ReviewVote newVote = new ReviewVote(voteId, voteType);
            voteRepository.save(newVote);
        }
    }
}
