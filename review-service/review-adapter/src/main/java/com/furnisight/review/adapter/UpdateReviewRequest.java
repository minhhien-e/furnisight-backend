package com.furnisight.review.adapter.web.rest.dto.request.review;

import java.util.UUID;

public record UpdateReviewRequest(
        UUID reviewId,
        String title,
        String content,
        Integer rating) {
}

