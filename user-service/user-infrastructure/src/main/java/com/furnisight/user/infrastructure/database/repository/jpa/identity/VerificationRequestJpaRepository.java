package com.furnisight.user.infrastructure.database.repository.jpa.identity;

import com.furnisight.user.domain.entities.identity.VerificationRequest;
import com.furnisight.user.domain.enums.identity.VerificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationRequestJpaRepository extends JpaRepository<VerificationRequest, UUID> {

    @Query("SELECT v FROM VerificationRequest v WHERE v.accountId = :accountId AND v.type = :type AND v.step <> 'COMPLETED' AND v.expiresAt > CURRENT_TIMESTAMP")
    Optional<VerificationRequest> findActiveByAccountIdAndType(@Param("accountId") UUID accountId, @Param("type") VerificationType type);

    @Query("SELECT v FROM VerificationRequest v WHERE v.otpCode = :otpCode AND v.type = :type AND v.step <> 'COMPLETED' AND v.expiresAt > CURRENT_TIMESTAMP")
    Optional<VerificationRequest> findByOtpCodeAndType(@Param("otpCode") String otpCode, @Param("type") VerificationType type);

    void deleteByAccountIdAndType(UUID accountId, VerificationType type);
}
