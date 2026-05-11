package com.furnisight.user.application.profile.dto;

import com.furnisight.user.domain.enums.identity.VerificationType;
import com.furnisight.user.domain.enums.identity.VerificationMethod;

import java.util.UUID;

/** Step 1: Request OTP sent to current email/phone to prove ownership. */
public record RequestContactChangeCommand(
        UUID accountId,
        VerificationType type,    // EMAIL_CHANGE | PHONE_CHANGE
        VerificationMethod verifyBy // Optional: EMAIL or PHONE
) {}
