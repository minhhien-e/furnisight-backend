package com.furnisight.promotion.application.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SaveMarketingCampaignCommand {
    private String name;
    private String voucherId;
    private String targetType;
    private List<String> targetUserIds;
    private String segmentKey;
    private List<String> channels;
    private String scheduleType;
    private LocalDateTime scheduledAt;
    private String notificationTitle;
    private String notificationBody;
    private Boolean active;
}
