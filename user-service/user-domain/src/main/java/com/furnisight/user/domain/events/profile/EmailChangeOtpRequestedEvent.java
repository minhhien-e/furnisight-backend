package com.furnisight.user.domain.events.profile;

import com.furnisight.user.domain.seedwork.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Fired when an OTP is generated for an EMAIL_CHANGE flow (step 2 - send to new email).
 * The {@code destination} field holds the new email address to send to.
 */
public record EmailChangeOtpRequestedEvent(
        UUID accountId,
        String destination,   // email address to send OTP to
        String otpCode,
        LocalDateTime occurredAt
) implements DomainEvent {
}
