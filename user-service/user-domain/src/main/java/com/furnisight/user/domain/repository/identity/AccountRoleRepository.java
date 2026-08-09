package com.furnisight.user.domain.repository.identity;

import com.furnisight.user.domain.entities.identity.AccountRole;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRoleRepository{
    AccountRole save(AccountRole accountRole);

    void delete(AccountRole accountRole);

    Optional<AccountRole> findByAccountIdAndRoleId(UUID accountId, UUID roleId);
}
