package com.furnisight.user.infrastructure.database.repository.impl.identity;

import com.furnisight.user.domain.entities.identity.AccountToken;
import com.furnisight.user.domain.repository.identity.AccountTokenRepository;
import com.furnisight.user.domain.valueobjects.identity.RefreshToken;
import com.furnisight.user.infrastructure.database.repository.jpa.identity.AccountTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AccountTokenRepositoryImpl implements AccountTokenRepository {
    private final AccountTokenJpaRepository accountTokenJpaRepository;
    @Override
    public AccountToken save(AccountToken accountToken) {
        return  accountTokenJpaRepository.save(accountToken);
    }

    @Override
    public Optional<AccountToken> findByRefreshToken(RefreshToken refreshToken) {
        return accountTokenJpaRepository.findByRefreshToken_Value(refreshToken.getValue());
    }

    @Override
    public List<AccountToken> findAllByAccountId(UUID accountId) {
        return accountTokenJpaRepository.findAllByAccountId(accountId);
    }
}
