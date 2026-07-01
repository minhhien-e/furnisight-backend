package com.furnisight.review.adapter.web.rest.dto.request.review;

import java.util.UUID;

public record CreateReviewRequest(
        String title,
        String productId,
        String orderItemId,
        String content,
        int rating,
        String userName,
        UUID userAvatarMediaId) {
}

