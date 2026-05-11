package com.furnisight.user.application.profile.dto;

import com.furnisight.user.domain.enums.identity.VerificationType;

import java.util.UUID;

public record ConfirmLinkContactCommand(
        UUID accountId,
        VerificationType type,
        String otpCode
) {}
