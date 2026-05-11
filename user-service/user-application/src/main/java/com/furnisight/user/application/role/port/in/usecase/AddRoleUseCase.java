package com.furnisight.user.application.role.port.in.usecase;

import com.furnisight.user.application.common.port.in.UseCase;
import com.furnisight.user.application.role.dto.command.AddRoleCommand;
import com.furnisight.user.domain.entities.identity.Role;

public interface AddRoleUseCase extends UseCase<AddRoleCommand, Role> {
}
