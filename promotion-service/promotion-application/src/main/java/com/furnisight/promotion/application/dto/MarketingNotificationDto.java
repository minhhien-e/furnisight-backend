package com.furnisight.promotion.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MarketingNotificationDto {
    private String id;
    private String title;
    private String body;
    private String targetType;
    private List<String> targetUserIds;
    private String targetLabel;
    private String segmentKey;
    private List<String> channels;
    private List<String> channelLabels;
    private String sendType;
    private LocalDateTime scheduledAt;
    private String relatedVoucherId;
    private String status;
    private long sentCount;
    private boolean active;
    private LocalDateTime createdAt;
}
