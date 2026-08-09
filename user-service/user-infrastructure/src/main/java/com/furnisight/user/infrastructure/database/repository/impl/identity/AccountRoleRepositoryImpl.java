package com.furnisight.user.infrastructure.database.repository.impl.identity;

import com.furnisight.user.domain.entities.identity.AccountRole;
import com.furnisight.user.domain.repository.identity.AccountRoleRepository;
import com.furnisight.user.infrastructure.database.repository.jpa.identity.AccountRoleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AccountRoleRepositoryImpl implements AccountRoleRepository {
    private final AccountRoleJpaRepository accountRoleJpaRepository;

    @Override
    public AccountRole save(AccountRole accountRole) {
        return accountRoleJpaRepository.save(accountRole);
    }

    @Override
    public void delete(AccountRole accountRole) {
        accountRoleJpaRepository.delete(accountRole);
    }

    @Override
    public Optional<AccountRole> findByAccountIdAndRoleId(UUID accountId, UUID roleId) {
        return accountRoleJpaRepository.findByAccountIdAndRoleId(accountId,roleId);
    }
}
