package com.furnisight.catalog.application.review.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewResponse(
    UUID id, 
    UUID userId, 
    String userName,
    String userAvatarUrl,
    UUID productId, 
    String title, 
    String content, 
    Integer rating, 
    String status, 
    LocalDateTime createdAt
) {}
