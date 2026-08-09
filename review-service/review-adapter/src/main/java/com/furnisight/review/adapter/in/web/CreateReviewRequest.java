package com.furnisight.review.adapter.in.web;

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

