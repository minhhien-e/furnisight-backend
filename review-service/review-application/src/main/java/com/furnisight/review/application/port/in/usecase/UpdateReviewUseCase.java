package com.furnisight.review.application.review.port.in.usecase;

import java.util.UUID;

public interface UpdateReviewUseCase {
    void updateReview(UUID reviewId, String title, String content, Integer rating);
}

