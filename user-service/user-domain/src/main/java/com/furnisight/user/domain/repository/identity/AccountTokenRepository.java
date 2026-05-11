package com.furnisight.user.domain.repository.identity;

import com.furnisight.user.domain.entities.identity.AccountToken;
import com.furnisight.user.domain.valueobjects.identity.RefreshToken;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountTokenRepository {
    AccountToken save(AccountToken accountToken);

    Optional<AccountToken> findByRefreshToken(RefreshToken refreshToken);

    List<AccountToken> findAllByAccountId(UUID accountId);
}
