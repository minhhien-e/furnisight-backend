package com.furnisight.user.domain.repository.identity;

import com.furnisight.user.domain.entities.identity.Role;
import com.furnisight.user.domain.valueobjects.identity.RoleName;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository {
    Optional<Role> findById(UUID id);
    Optional<Role> findByName(RoleName name);
    Role save(Role role);
    void delete(Role role);

    List<Role> findAllByAccountId(UUID id);
}
