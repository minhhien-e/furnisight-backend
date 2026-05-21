package com.furnisight.catalog.presentation.web.rest.dto.request.review;

public record CreateReviewRequest(
        String title,
        String productId,
        String orderItemId,
        String content,
        int rating) {
}
