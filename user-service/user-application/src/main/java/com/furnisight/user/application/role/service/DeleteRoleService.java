package com.furnisight.user.application.role.service;

import com.furnisight.user.application.role.dto.command.DeleteRoleCommand;
import com.furnisight.user.application.role.port.in.usecase.DeleteRoleUseCase;
import com.furnisight.user.domain.entities.identity.Role;
import com.furnisight.user.domain.exceptions.identity.ErrorCode;
import com.furnisight.user.domain.exceptions.identity.NotFoundException;
import com.furnisight.user.domain.repository.identity.RoleRepository;
import com.furnisight.user.domain.services.identity.role.RoleLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteRoleService implements DeleteRoleUseCase {

    private final RoleRepository roleRepository;
    private final RoleLifecycleService roleLifecycleService;

    @Override
    @Transactional
    public Void execute(DeleteRoleCommand command) {
        Role role = roleRepository.findById(command.roleId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.ROLE_NOT_FOUND));

        roleLifecycleService.deleteRole(role);
        return null;
    }
}
