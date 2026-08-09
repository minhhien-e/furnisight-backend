package com.furnisight.review.adapter.in.web;

import java.util.UUID;

public record UpdateReviewRequest(
        UUID reviewId,
        String title,
        String content,
        Integer rating) {
}

