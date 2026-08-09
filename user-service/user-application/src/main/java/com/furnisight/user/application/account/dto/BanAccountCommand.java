package com.furnisight.user.application.account.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record BanAccountCommand(
        UUID adminId,
        UUID targetAccountId,
        String reason,
        LocalDateTime expiresAt
) {}
