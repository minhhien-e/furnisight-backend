package com.furnisight.user.domain.events.profile;

import com.furnisight.user.domain.seedwork.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Fired when an OTP is generated for linking a new email address.
 */
public record EmailLinkOtpRequestedEvent(
        UUID accountId,
        String destination,   // email address to send OTP to
        String otpCode,
        LocalDateTime occurredAt
) implements DomainEvent {
}
