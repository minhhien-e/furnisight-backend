package com.furnisight.promotion.application.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SaveMarketingNotificationCommand {
    private String title;
    private String body;
    private String targetType;
    private List<String> targetUserIds;
    private String segmentKey;
    private List<String> channels;
    private String sendType;
    private LocalDateTime scheduledAt;
    private String relatedVoucherId;
    private Boolean active;
}
