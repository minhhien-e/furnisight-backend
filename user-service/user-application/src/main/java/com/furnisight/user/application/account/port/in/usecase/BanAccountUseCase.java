package com.furnisight.user.application.account.port.in.usecase;

import com.furnisight.user.application.account.dto.BanAccountCommand;
import com.furnisight.user.application.common.port.in.UseCase;
import com.furnisight.user.domain.entities.identity.Ban;

public interface BanAccountUseCase extends UseCase<BanAccountCommand, Ban> {
}
