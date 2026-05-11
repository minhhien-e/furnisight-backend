package com.furnisight.user.domain.services.identity.generator;

import com.furnisight.user.domain.valueobjects.identity.RefreshToken;

public interface RefreshTokenGenerator {
    RefreshToken generateToken();
}
