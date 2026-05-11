package com.furnisight.user.infrastructure.database.repository.jpa.identity;

import com.furnisight.user.domain.entities.identity.AccountRole;
import com.furnisight.user.domain.entities.identity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRoleJpaRepository extends JpaRepository<AccountRole, UUID> {
    Optional<AccountRole> findByAccountIdAndRoleId(UUID accountId, UUID roleId);

    List<AccountRole> findAllByAccountId(UUID accountId);
}
