package com.furnisight.user.application.role.service;

import com.furnisight.user.application.role.dto.command.AddRoleCommand;
import com.furnisight.user.application.role.port.in.usecase.AddRoleUseCase;
import com.furnisight.user.domain.entities.identity.Role;
import com.furnisight.user.domain.services.identity.role.RoleLifecycleService;
import com.furnisight.user.domain.valueobjects.identity.RoleName;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddRoleService implements AddRoleUseCase {

    private final RoleLifecycleService roleLifecycleService;

    @Override
    @Transactional
    public Role execute(AddRoleCommand command) {
        RoleName roleName = new RoleName(command.name());
        return roleLifecycleService.addRole(roleName, command.position());
    }
}
