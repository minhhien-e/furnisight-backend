package com.furnisight.admin.marketing.web.dto;

import java.time.LocalDateTime;
import java.util.List;

public record MarketingNotificationResponse(
        String id,
        String title,
        String body,
        String targetType,
        List<String> targetUserIds,
        String targetLabel,
        String segmentKey,
        List<String> channels,
        List<String> channelLabels,
        String sendType,
        LocalDateTime scheduledAt,
        String relatedVoucherId,
        String status,
        Long sentCount,
        Boolean active,
        LocalDateTime createdAt
) {
}
