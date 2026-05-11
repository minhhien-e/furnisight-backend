package com.furnisight.user.application.token.port.in.usecase;

import com.furnisight.user.application.common.port.in.UseCase;
import com.furnisight.user.application.token.dto.RenewAccessTokenCommand;
import com.furnisight.user.domain.entities.identity.AccountToken;

public interface RenewAccessTokenUseCase extends UseCase<RenewAccessTokenCommand, AccountToken> {
}
