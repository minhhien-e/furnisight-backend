package com.furnisight.user.application.role.service;

import com.furnisight.user.application.role.dto.command.RevokeRolePermissionCommand;
import com.furnisight.user.application.role.port.in.usecase.RevokeRolePermissionUseCase;
import com.furnisight.user.domain.entities.identity.Role;
import com.furnisight.user.domain.enums.identity.Permission;
import com.furnisight.user.domain.exceptions.identity.ErrorCode;
import com.furnisight.user.domain.exceptions.identity.NotFoundException;
import com.furnisight.user.domain.repository.identity.RoleRepository;
import com.furnisight.user.domain.services.identity.role.RolePermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RevokeRolePermissionService implements RevokeRolePermissionUseCase {

    private final RoleRepository roleRepository;
    private final RolePermissionService rolePermissionService;

    @Override
    @Transactional
    public Void execute(RevokeRolePermissionCommand command) {
        Role role = roleRepository.findById(command.roleId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.ROLE_NOT_FOUND));

        Permission permission = Permission.valueOf(command.permission().toUpperCase());
        rolePermissionService.revokePermission(role, permission);

        return null;
    }
}
