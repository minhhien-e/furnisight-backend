package com.furnisight.user.presentation.web.rest.dto.request.identiy;

import java.time.LocalDateTime;

public record BanAccountRequest(
        String reason,
        LocalDateTime expiresAt
) {}
