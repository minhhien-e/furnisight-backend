package com.furnisight.admin.marketing.web.dto;

import java.time.LocalDateTime;
import java.util.List;

public record MarketingNotificationRequest(
        String title,
        String body,
        String targetType,
        List<String> targetUserIds,
        String segmentKey,
        List<String> channels,
        String sendType,
        LocalDateTime scheduledAt,
        String relatedVoucherId,
        Boolean active
) {
}
