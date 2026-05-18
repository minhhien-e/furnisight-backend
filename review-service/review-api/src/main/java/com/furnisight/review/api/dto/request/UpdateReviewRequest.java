package com.furnisight.review.api.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record UpdateReviewRequest(
    @NotNull UUID reviewId,
    String title,
    String content,
    Integer rating,
    String ipAddress
) {
    public String ipAddress() {
        return ipAddress;
    }
}
