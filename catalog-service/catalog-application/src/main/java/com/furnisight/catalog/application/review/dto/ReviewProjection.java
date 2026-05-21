package com.furnisight.catalog.application.review.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewProjection(
    UUID id, 
    UUID userId, 
    UUID productId, 
    String title, 
    String content, 
    Integer rating, 
    String status, 
    LocalDateTime createdAt
) {}
