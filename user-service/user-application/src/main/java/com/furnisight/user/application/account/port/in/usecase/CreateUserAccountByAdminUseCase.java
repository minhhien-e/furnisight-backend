package com.furnisight.user.application.account.port.in.usecase;

import com.furnisight.user.application.account.dto.CreateAccountCommand;
import com.furnisight.user.application.common.port.in.UseCase;

public interface CreateUserAccountByAdminUseCase extends UseCase<CreateAccountCommand, Void> {
}
