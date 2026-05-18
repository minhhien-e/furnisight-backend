package com.furnisight.review.api.dto.request;

import jakarta.validation.constraints.*;

public record CreateReviewRequest(
    @NotBlank String title,
    @NotBlank String userId,
    @NotBlank String productId,
    @NotBlank String orderItemId,
    @NotBlank String content,
    @Min(1) @Max(5) int rating
) {}
