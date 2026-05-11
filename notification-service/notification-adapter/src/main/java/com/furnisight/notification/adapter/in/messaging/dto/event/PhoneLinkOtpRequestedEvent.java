package com.furnisight.notification.adapter.in.messaging.dto.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record PhoneLinkOtpRequestedEvent(
    UUID accountId,
    String destination,
    String otpCode,
    LocalDateTime occurredAt
) {
}
