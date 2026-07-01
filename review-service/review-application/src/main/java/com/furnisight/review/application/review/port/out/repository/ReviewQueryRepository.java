package com.furnisight.review.application.review.port.out.repository;

import com.furnisight.review.application.review.dto.response.ProductReviewStatResponse;
import com.furnisight.review.application.review.dto.response.ReviewResponse;
import com.furnisight.review.application.review.dto.response.ReviewSentimentStatsResponse;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ReviewQueryRepository {
    List<ReviewResponse> findByProductId(UUID productId, Integer page, Integer size);
    List<ReviewResponse> findByUserIdAndOrderItemIds(UUID userId, Collection<UUID> orderItemIds);
    List<ReviewResponse> findTopRandomReviews(int limit);
    List<ProductReviewStatResponse> findProductStats(Collection<UUID> productIds);
    ReviewSentimentStatsResponse findReviewSentimentStats();
}

