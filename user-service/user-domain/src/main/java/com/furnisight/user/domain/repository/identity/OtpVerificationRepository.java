package com.furnisight.user.domain.repository.identity;

import com.furnisight.user.domain.enums.identity.VerificationType;

import java.time.Duration;
import java.util.Optional;

public interface OtpVerificationRepository {
    void save(String email, VerificationType type, String otpHash, Duration ttl);

    Optional<String> findHashByEmailAndType(String email, VerificationType type);

    void deleteByEmailAndType(String email, VerificationType type);
}
