package com.furnisight.catalog.application.review.port.in.usecase;

import java.util.UUID;

public interface DeleteReviewUseCase {
    void deleteReview(UUID reviewId);
}
