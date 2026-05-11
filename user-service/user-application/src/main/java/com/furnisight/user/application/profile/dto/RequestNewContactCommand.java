package com.furnisight.user.application.profile.dto;

import com.furnisight.user.domain.enums.identity.VerificationType;

import java.util.UUID;

/** Step 2: Submit the new email/phone and receive OTP to that new contact. */
public record RequestNewContactCommand(
        UUID accountId,
        VerificationType type,
        String newContact   // new email or new phone number
) {}
