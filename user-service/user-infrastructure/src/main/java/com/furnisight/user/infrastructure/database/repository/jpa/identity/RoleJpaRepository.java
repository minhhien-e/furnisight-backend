package com.furnisight.user.infrastructure.database.repository.jpa.identity;

import com.furnisight.user.domain.entities.identity.Role;
import com.furnisight.user.domain.valueobjects.identity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleJpaRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(RoleName name);

    List<Role> findAllByIdIn(List<UUID> roleIds);
}
