package com.furnisight.review.application.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private UUID id;
    private UUID userId;
    private String userName;
    private String userAvatarUrl;
    private UUID productId;
    private UUID orderItemId;
    private String title;
    private String content;
    private Integer rating;
    private String status;
    private String sentiment;
    private LocalDateTime createdAt;
}
