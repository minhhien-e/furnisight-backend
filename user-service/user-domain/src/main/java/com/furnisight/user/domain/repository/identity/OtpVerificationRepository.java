package com.furnisight.user.domain.repository.identity;

import com.furnisight.user.domain.enums.identity.VerificationType;

import java.time.Duration;
import java.util.Optional;

public interface OtpVerificationRepository {
    void save(String email, VerificationType type, String otpHash, Duration ttl);

    Optional<String> findEmailByHashAndType(String otpHash, VerificationType type);

    void deleteByHashAndType(String otpHash, VerificationType type);
}
