package com.furnisight.user.presentation.web.rest.dto.response;

import com.furnisight.user.domain.entities.identity.AccountToken;

public record AuthResponse(
    String accessToken,
    String refreshToken
) {
    public static AuthResponse from(AccountToken accountToken) {
        return new AuthResponse(
            accountToken.getAccessToken().getValue(),
            accountToken.getRefreshToken().getValue()
        );
    }
}
