package com.furnisight.user.infrastructure.security.hasher;

import com.furnisight.user.domain.services.identity.account.PasswordHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BCryptHasher implements PasswordHasher {
    private final PasswordEncoder passwordEncoder;

    @Override
    public String hash(String password) {
        return passwordEncoder.encode(password);
    }

    @Override
    public boolean verify(String password, String hashedPassword) {
        return passwordEncoder.matches(password, hashedPassword);
    }
}
