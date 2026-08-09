package com.furnisight.user.domain.repository.identity;

import com.furnisight.user.domain.entities.identity.Ban;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

public interface BanRepository {
    Optional<Ban> findByAccountIdAndIsActiveTrue(UUID accountId);

    Ban save(Ban ban);

    void disableAllByAccountId(UUID accountId);
}
