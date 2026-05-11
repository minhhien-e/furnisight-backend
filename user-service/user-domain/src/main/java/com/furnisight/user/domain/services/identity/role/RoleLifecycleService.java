package com.furnisight.user.domain.services.identity.role;

import com.furnisight.user.domain.entities.identity.Role;
import com.furnisight.user.domain.enums.identity.Permission;
import com.furnisight.user.domain.repository.identity.RoleRepository;
import com.furnisight.user.domain.valueobjects.identity.RoleName;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleLifecycleService {
    private final RoleRepository roleRepository;

    public Role addRole(RoleName name, int position) {
        Role role = new Role(name, position);
        return roleRepository.save(role);
    }

    public Role updateRole(Role role, RoleName newName, int newPosition) {
        role.update(newName, newPosition);
        return role;
    }

    public void deleteRole(Role role) {
        roleRepository.delete(role);
    }
}
