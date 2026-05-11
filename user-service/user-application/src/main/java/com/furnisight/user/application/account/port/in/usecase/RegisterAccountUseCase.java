package com.furnisight.user.application.account.port.in.usecase;

import com.furnisight.user.application.account.dto.RegisterAccountCommand;
import com.furnisight.user.application.common.port.in.UseCase;
import com.furnisight.user.domain.entities.identity.AccountToken;

public interface RegisterAccountUseCase extends UseCase<RegisterAccountCommand, AccountToken> {
}
