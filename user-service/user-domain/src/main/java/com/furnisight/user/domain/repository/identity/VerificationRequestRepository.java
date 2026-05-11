package com.furnisight.user.domain.repository.identity;

import com.furnisight.user.domain.entities.identity.VerificationRequest;
import com.furnisight.user.domain.enums.identity.VerificationType;

import java.util.Optional;
import java.util.UUID;

public interface VerificationRequestRepository {
    VerificationRequest save(VerificationRequest request);

    Optional<VerificationRequest> findActiveByAccountIdAndType(UUID accountId, VerificationType type);

    Optional<VerificationRequest> findByOtpCodeAndType(String otpCode, VerificationType type);

    void deleteByAccountIdAndType(UUID accountId, VerificationType type);
}
