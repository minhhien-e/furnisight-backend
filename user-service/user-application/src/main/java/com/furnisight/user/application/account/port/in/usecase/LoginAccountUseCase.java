package com.furnisight.user.application.account.port.in.usecase;

import com.furnisight.user.application.account.dto.LoginAccountCommand;
import com.furnisight.user.application.common.port.in.UseCase;
import com.furnisight.user.domain.entities.identity.AccountToken;

public interface LoginAccountUseCase extends UseCase<LoginAccountCommand, AccountToken> {
}
