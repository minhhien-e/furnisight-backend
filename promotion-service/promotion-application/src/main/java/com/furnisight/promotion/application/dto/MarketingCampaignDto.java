package com.furnisight.promotion.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MarketingCampaignDto {
    private String id;
    private String name;
    private String voucherId;
    private String voucherCode;
    private String targetType;
    private List<String> targetUserIds;
    private String targetLabel;
    private String segmentKey;
    private List<String> channels;
    private List<String> channelLabels;
    private String scheduleType;
    private LocalDateTime scheduledAt;
    private String notificationTitle;
    private String notificationBody;
    private String status;
    private long sentCount;
    private boolean active;
    private LocalDateTime createdAt;
}
