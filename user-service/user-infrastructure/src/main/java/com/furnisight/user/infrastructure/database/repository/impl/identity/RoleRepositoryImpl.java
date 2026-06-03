package com.furnisight.user.infrastructure.database.repository.impl.identity;

import com.furnisight.user.domain.entities.identity.AccountRole;
import com.furnisight.user.domain.entities.identity.Role;
import com.furnisight.user.domain.repository.identity.RoleRepository;
import com.furnisight.user.domain.valueobjects.identity.RoleName;
import com.furnisight.user.infrastructure.database.repository.jpa.identity.AccountRoleJpaRepository;
import com.furnisight.user.infrastructure.database.repository.jpa.identity.RoleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RoleRepositoryImpl implements RoleRepository {
    private final RoleJpaRepository roleJpaRepository;
    private final AccountRoleJpaRepository accountRoleJpaRepository;

    @Override
    public Optional<Role> findById(UUID id) {
        return roleJpaRepository.findById(id);
    }

    @Override
    public Optional<Role> findByName(RoleName name) {
        return roleJpaRepository.findByName(name);
    }

    @Override
    public Role save(Role role) {
        return roleJpaRepository.save(role);
    }

    @Override
    public void delete(Role role) {
        roleJpaRepository.delete(role);
    }

    @Override
    public List<Role> findAll() {
        return roleJpaRepository.findAll();
    }

    @Override
    public List<Role> findAllByAccountId(UUID accountId) {
        List<AccountRole> accountRoles = accountRoleJpaRepository.findAllByAccountId(accountId);
        List<UUID> roleIds = accountRoles.stream().map(AccountRole::getRoleId).toList();
        return roleJpaRepository.findAllByIdIn(roleIds);
    }
}
