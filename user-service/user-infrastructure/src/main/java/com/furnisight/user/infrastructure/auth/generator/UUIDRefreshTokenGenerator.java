package com.furnisight.user.infrastructure.auth.generator;

import com.furnisight.user.domain.services.identity.generator.RefreshTokenGenerator;
import com.furnisight.user.domain.valueobjects.identity.RefreshToken;
import com.furnisight.user.infrastructure.security.config.JwtConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UUIDRefreshTokenGenerator implements RefreshTokenGenerator {

    private final JwtConfig jwtConfig;

    @Override
    public RefreshToken generateToken() {
        String tokenValue = UUID.randomUUID().toString();
        LocalDateTime expirationDateTime = LocalDateTime.now()
                .plusSeconds(jwtConfig.getRefreshTokenExpirationMs() / 1000);
        return new RefreshToken(tokenValue, expirationDateTime, false);
    }
}
