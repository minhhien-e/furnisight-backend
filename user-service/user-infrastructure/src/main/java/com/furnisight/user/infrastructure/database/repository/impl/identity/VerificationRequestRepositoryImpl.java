package com.furnisight.user.infrastructure.database.repository.impl.identity;

import com.furnisight.user.domain.entities.identity.VerificationRequest;
import com.furnisight.user.domain.enums.identity.VerificationType;
import com.furnisight.user.domain.repository.identity.VerificationRequestRepository;
import com.furnisight.user.infrastructure.database.repository.jpa.identity.VerificationRequestJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class VerificationRequestRepositoryImpl implements VerificationRequestRepository {

    private final VerificationRequestJpaRepository jpaRepository;

    @Override
    public VerificationRequest save(VerificationRequest request) {
        return jpaRepository.save(request);
    }

    @Override
    public Optional<VerificationRequest> findActiveByAccountIdAndType(UUID accountId, VerificationType type) {
        return jpaRepository.findActiveByAccountIdAndType(accountId, type);
    }

    @Override
    public Optional<VerificationRequest> findByOtpCodeAndType(String otpCode, VerificationType type) {
        return jpaRepository.findByOtpCodeAndType(otpCode, type);
    }

    @Override
    public void deleteByAccountIdAndType(UUID accountId, VerificationType type) {
        jpaRepository.deleteByAccountIdAndType(accountId, type);
    }
}
