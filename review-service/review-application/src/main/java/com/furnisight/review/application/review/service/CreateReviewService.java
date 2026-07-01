package com.furnisight.review.application.review.service;

import com.furnisight.review.application.review.port.out.ReviewEligibilityPort;
import com.furnisight.review.application.review.port.in.usecase.CreateReviewUseCase;
import com.furnisight.review.application.review.port.out.ReviewEventPublisherPort;
import com.furnisight.review.application.review.port.out.repository.ReviewWritePort;
import com.furnisight.review.domain.entities.Review;
import com.furnisight.review.domain.exceptions.AlreadyExistsException;
import com.furnisight.review.domain.exceptions.ErrorCode;
import com.furnisight.review.domain.exceptions.ForbiddenException;
import com.furnisight.review.domain.valueobjects.review.ReviewContent;
import com.furnisight.review.domain.valueobjects.review.ReviewTitle;
import com.furnisight.review.domain.valueobjects.review.StarRating;
import com.furnisight.review.domain.services.review.ProfanityPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.furnisight.review.application.review.port.out.ReviewSentimentTriggerPort;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateReviewService implements CreateReviewUseCase {

    private final ReviewWritePort reviewWritePort;
    private final ProfanityPolicy profanityPolicy;
    private final ReviewEligibilityPort reviewEligibilityPort;
    private final ReviewSentimentTriggerPort reviewSentimentTriggerPort;
    private final ReviewEventPublisherPort reviewEventPublisherPort;

    @Override
    @Transactional
    public void createReview(
            UUID userId,
            String productId,
            String orderItemId,
            String title,
            String content,
            Integer rating,
            String userName,
            UUID userAvatarMediaId
    ) {
        UUID productUuid = UUID.fromString(productId);
        UUID orderItemUuid = UUID.fromString(orderItemId);

        if (reviewWritePort.findByOrderItemId(orderItemUuid).isPresent()) {
            throw new AlreadyExistsException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }
        if (!reviewEligibilityPort.isEligible(userId, productUuid, orderItemUuid)) {
            throw new ForbiddenException(ErrorCode.ORDER_ITEM_NOT_ELIGIBLE);
        }

        Review review = new Review(
            new ReviewTitle(title),
            userId,
            productUuid,
            orderItemUuid,
            ReviewContent.from(content),
            new StarRating(rating)
        );
        review.updateUserInfo(normalizeUserName(userName), userAvatarMediaId);
        review.applyModeration(profanityPolicy);

        reviewWritePort.save(review);
        reviewSentimentTriggerPort.triggerSentimentAnalysis(review.getId(), content);
        reviewEventPublisherPort.publishReviewChangedEvent(productUuid);
    }

    private String normalizeUserName(String userName) {
        if (userName == null || userName.isBlank()) {
            return null;
        }
        return userName.trim();
    }
}

