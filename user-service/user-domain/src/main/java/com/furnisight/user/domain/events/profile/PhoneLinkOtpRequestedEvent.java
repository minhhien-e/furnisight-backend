package com.furnisight.user.domain.events.profile;

import com.furnisight.user.domain.seedwork.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Fired when an OTP is generated for linking a new phone number.
 */
public record PhoneLinkOtpRequestedEvent(
        UUID accountId,
        String destination,   // phone number to send OTP to
        String otpCode,
        LocalDateTime occurredAt
) implements DomainEvent {
}
