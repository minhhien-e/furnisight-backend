package com.furnisight.user.application.profile.dto;

import com.furnisight.user.domain.enums.identity.VerificationType;

import java.util.UUID;

/** Step 1 verify: Submit OTP received at current email/phone. */
public record VerifyCurrentContactCommand(
        UUID accountId,
        VerificationType type,
        String otpCode
) {}
