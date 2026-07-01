package com.furnisight.admin.marketing.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public record MarketingCampaignRequest(
        @NotBlank(message = "Name is required") String name,
        @NotBlank(message = "Voucher ID is required") String voucherId,
        @NotBlank(message = "Target type is required") String targetType,
        List<String> targetUserIds,
        String segmentKey,
        @NotNull(message = "Channels cannot be null") List<String> channels,
        @NotBlank(message = "Schedule type is required") String scheduleType,
        LocalDateTime scheduledAt,
        @NotBlank(message = "Notification title is required") String notificationTitle,
        @NotBlank(message = "Notification body is required") String notificationBody,
        Boolean active
) {
}
