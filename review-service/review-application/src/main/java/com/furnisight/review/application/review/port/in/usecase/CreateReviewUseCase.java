package com.furnisight.review.application.review.port.in.usecase;

import java.util.UUID;

public interface CreateReviewUseCase {
    void createReview(
        UUID userId,
        String productId,
        String orderItemId,
        String title,
        String content,
        Integer rating,
        String userName,
        UUID userAvatarMediaId
    );
}

