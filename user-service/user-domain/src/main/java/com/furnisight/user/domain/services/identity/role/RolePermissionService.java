package com.furnisight.user.domain.services.identity.role;

import com.furnisight.user.domain.entities.identity.Role;
import com.furnisight.user.domain.enums.identity.Permission;
import org.springframework.stereotype.Service;

@Service
public class RolePermissionService {
    public void addPermission(Role role, Permission permission) {
        role.grantPermission(permission);
    }

    public void revokePermission(Role role, Permission permission) {
        role.revokePermission(permission);
    }
}
