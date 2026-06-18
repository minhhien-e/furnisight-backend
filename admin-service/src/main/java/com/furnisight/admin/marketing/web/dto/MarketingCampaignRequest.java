package com.furnisight.admin.marketing.web.dto;

import java.time.LocalDateTime;
import java.util.List;

public record MarketingCampaignRequest(
        String name,
        String voucherId,
        String targetType,
        List<String> targetUserIds,
        String segmentKey,
        List<String> channels,
        String scheduleType,
        LocalDateTime scheduledAt,
        String notificationTitle,
        String notificationBody,
        Boolean active
) {
}
