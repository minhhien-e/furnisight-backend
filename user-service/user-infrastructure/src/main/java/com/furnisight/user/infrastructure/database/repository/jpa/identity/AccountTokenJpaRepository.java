package com.furnisight.user.infrastructure.database.repository.jpa.identity;

import com.furnisight.user.domain.entities.identity.AccountToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountTokenJpaRepository extends JpaRepository<AccountToken, UUID> {
    Optional<AccountToken> findByRefreshToken_Value(String value);

    List<AccountToken> findAllByAccountId(UUID accountId);
}
