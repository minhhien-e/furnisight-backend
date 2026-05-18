package com.furnisight.review.core.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewResponse(UUID id, UUID userId, UUID productId, String title, String content, Integer rating, String status, LocalDateTime createdAt
) {}
