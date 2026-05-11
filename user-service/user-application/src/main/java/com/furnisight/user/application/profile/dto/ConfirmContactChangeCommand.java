package com.furnisight.user.application.profile.dto;

import com.furnisight.user.domain.enums.identity.VerificationType;

import java.util.UUID;

/** Step 2 confirm: Submit OTP received at the NEW email/phone → completes the change. */
public record ConfirmContactChangeCommand(
        UUID accountId,
        VerificationType type,
        String otpCode
) {}
