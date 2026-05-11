package com.furnisight.user.domain.events.profile;

import com.furnisight.user.domain.seedwork.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Fired when an OTP is generated for a PHONE_CHANGE flow (step 2 - send to new phone).
 * The {@code destination} field holds the new phone number to send to.
 */
public record PhoneChangeOtpRequestedEvent(
        UUID accountId,
        String destination,   // phone number to send OTP to
        String otpCode,
        LocalDateTime occurredAt
) implements DomainEvent {
}
