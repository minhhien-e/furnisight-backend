package com.furnisight.catalog.application.review.service;

import com.furnisight.catalog.application.review.port.in.usecase.CreateReviewUseCase;
import com.furnisight.catalog.application.review.port.out.repository.ReviewWritePort;
import com.furnisight.catalog.domain.entities.Review;
import com.furnisight.catalog.domain.valueobjects.review.ReviewContent;
import com.furnisight.catalog.domain.valueobjects.review.ReviewTitle;
import com.furnisight.catalog.domain.valueobjects.review.StarRating;
import com.furnisight.catalog.domain.services.review.ProfanityPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateReviewService implements CreateReviewUseCase {

    private final ReviewWritePort reviewWritePort;
    private final ProfanityPolicy profanityPolicy;

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
        Review review = new Review(
            new ReviewTitle(title),
            userId,
            UUID.fromString(productId),
            UUID.fromString(orderItemId),
            ReviewContent.from(content),
            new StarRating(rating)
        );
        review.updateUserInfo(normalizeUserName(userName), userAvatarMediaId);
        review.applyModeration(profanityPolicy);

        reviewWritePort.save(review);
    }

    private String normalizeUserName(String userName) {
        if (userName == null || userName.isBlank()) {
            return null;
        }
        return userName.trim();
    }
}
