package com.furnisight.review.core.service.Review;

import com.furnisight.review.core.dto.ReviewResponse;
import com.furnisight.review.core.model.entity.Review;
import com.furnisight.review.core.policy.ProfanityPolicy;
import com.furnisight.review.core.repository.ReviewWritePort;
import com.furnisight.review.core.repository.ReviewQueryRepository;
import com.furnisight.review.core.model.valueobject.ReviewContent;
import com.furnisight.review.core.model.valueobject.ReviewTitle;
import com.furnisight.review.core.model.valueobject.StarRating;
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
    private final ProfanityPolicy profanityPolicy;
    private final ReviewQueryRepository reviewQueryRepository;

    @Transactional
    public void createReview(UUID userId, String productId, String orderItemId, String title, String content, Integer rating) {
        Review review = new Review(
            new ReviewTitle(title),
            userId,
            UUID.fromString(productId),
            UUID.fromString(orderItemId),
            ReviewContent.from(content),
            new StarRating(rating)
        );
        review.applyModeration(profanityPolicy);

        reviewWritePort.save(review);
    }

    @Transactional
    public void updateReview(UUID reviewId, String title, String content, Integer rating) {
        Review review = reviewWritePort.findById(reviewId)
            .orElseThrow(() -> new ReviewDomainException(ErrorCode.REVIEW_NOT_FOUND));
        boolean isChanged = review.updateFromUser(title, content, rating);

        if (isChanged) {
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
