package com.furnisight.user.application.account.port.in.usecase;

import com.furnisight.user.application.account.dto.AssignRoleCommand;
import com.furnisight.user.application.common.port.in.UseCase;
import com.furnisight.user.domain.entities.identity.AccountRole;

public interface AssignRoleUseCase extends UseCase<AssignRoleCommand, AccountRole> {
}
