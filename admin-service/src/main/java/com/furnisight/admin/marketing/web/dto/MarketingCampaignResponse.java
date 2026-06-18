package com.furnisight.admin.marketing.web.dto;

import java.time.LocalDateTime;
import java.util.List;

public record MarketingCampaignResponse(
        String id,
        String name,
        String voucherId,
        String voucherCode,
        String targetType,
        List<String> targetUserIds,
        String targetLabel,
        String segmentKey,
        List<String> channels,
        List<String> channelLabels,
        String scheduleType,
        LocalDateTime scheduledAt,
        String notificationTitle,
        String notificationBody,
        String status,
        Long sentCount,
        Boolean active,
        LocalDateTime createdAt
) {
}
