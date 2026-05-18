package com.furnisight.review.core.service.Review;

import com.furnisight.review.core.dto.ReviewResponse;
import com.furnisight.review.core.model.entity.Review;
import com.furnisight.review.core.model.entity.ReviewProcessingJob;
import com.furnisight.review.core.model.entity.ReviewEditHistory;
import com.furnisight.review.core.model.enums.ActorType;
import com.furnisight.review.core.model.enums.ModerationReason;
import com.furnisight.review.core.model.enums.ReviewJobStatus;
import com.furnisight.review.core.model.enums.ReviewJobType;
import com.furnisight.review.core.policy.ProfanityPolicy;
import com.furnisight.review.core.repository.ReviewJobWritePort;
import com.furnisight.review.core.repository.ReviewWritePort;
import com.furnisight.review.core.repository.ReviewQueryRepository;
import com.furnisight.review.core.model.valueobject.ReviewContent;
import com.furnisight.review.core.model.valueobject.ReviewTitle;
import com.furnisight.review.core.model.valueobject.StarRating;
import com.furnisight.review.core.model.valueobject.ClientIp;
import com.furnisight.review.core.repository.ReviewEditHistoryRepository;
import com.furnisight.review.core.exception.ReviewDomainException;
import com.furnisight.review.core.exception.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewWritePort reviewWritePort;
    private final ReviewJobWritePort jobWritePort;
    private final ProfanityPolicy profanityPolicy;
    private final ReviewEditHistoryRepository historyRepository;
    private final ReviewQueryRepository reviewQueryRepository;

    @Transactional
    public void createReview(String userId, String productId, String orderItemId, String title, String content, Integer rating) {
        Review review = new Review(
            new ReviewTitle(title),
            UUID.fromString(userId),
            UUID.fromString(productId),
            UUID.fromString(orderItemId),
            ReviewContent.from(content),
            new StarRating(rating)
        );
        boolean hasProfanity = review.applyModeration(profanityPolicy);

        reviewWritePort.save(review);

        ReviewProcessingJob job = ReviewProcessingJob.builder()
            .reviewId(review.getId())
            .jobType(ReviewJobType.MODERATION_ACTION)
            .status(ReviewJobStatus.PENDING)
            .triggeredBy(ActorType.SYSTEM)
            .triggerActorId("REVIEW_CREATION_FLOW")
            .moderationReason(hasProfanity ? ModerationReason.PROFANITY : ModerationReason.NONE)
            .payload(String.format(
                "{\"initial_status\":\"%s\",\"has_profanity\":%b}",
                review.getStatus(),
                hasProfanity
            ))
            .retryCount(0)
            .build();

        jobWritePort.save(job);
    }

    @Transactional
    public void updateReview(UUID reviewId, String title, String content, Integer rating, String ipAddress) {
        Review review = reviewWritePort.findById(reviewId)
            .orElseThrow(() -> new ReviewDomainException(ErrorCode.REVIEW_NOT_FOUND));
        ReviewContent oldContent = review.getContent();
        StarRating oldRating = review.getRating();
        boolean isChanged = review.updateFromUser(title, content, rating);

        if (isChanged) {
            ReviewEditHistory history = new ReviewEditHistory(
                review.getId(),
                oldContent,
                review.getContent(),
                oldRating,
                review.getRating(),
                new ClientIp(ipAddress)
            );

            historyRepository.save(history);
            reviewWritePort.save(review);
        }
    }

    @Transactional
    public void deleteReview(UUID reviewId) {
        Review review = reviewWritePort.findById(reviewId)
            .orElseThrow(() -> new ReviewDomainException(
                ErrorCode.REVIEW_NOT_FOUND,
                Map.of("id", reviewId)
            ));

        reviewWritePort.deleteById(review.getId());
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByProduct(UUID productId, Integer page, Integer size) {
        return reviewQueryRepository.findByProductId(productId, page, size);
    }
}
