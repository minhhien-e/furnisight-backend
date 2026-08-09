package com.furnisight.admin.marketing.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public record MarketingNotificationRequest(
        @NotBlank(message = "Title is required") String title,
        @NotBlank(message = "Body is required") String body,
        @NotBlank(message = "Target type is required") String targetType,
        List<String> targetUserIds,
        String segmentKey,
        @NotNull(message = "Channels cannot be null") List<String> channels,
        @NotBlank(message = "Send type is required") String sendType,
        LocalDateTime scheduledAt,
        String relatedVoucherId,
        Boolean active
) {
}
