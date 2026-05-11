package com.furnisight.user.infrastructure.database.repository.jpa.identity;

import com.furnisight.user.domain.entities.identity.Ban;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface BanJpaRepository extends JpaRepository<Ban, UUID> {
    Optional<Ban> findByAccountIdAndIsActiveTrue(UUID accountId);

    @Modifying
    @Query("update  Ban b set b.isActive = false, b.expiresAt = null  where b.accountId = ?1")
    void disableAllByAccountId(UUID accountId);
}
