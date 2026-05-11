package com.furnisight.user.application.account.port.in.usecase;

import com.furnisight.user.application.account.dto.LoginWithSocialAccountCommand;
import com.furnisight.user.domain.entities.identity.AccountToken;

public interface LoginWithSocialAccountUseCase {
    AccountToken execute(LoginWithSocialAccountCommand command);
}
