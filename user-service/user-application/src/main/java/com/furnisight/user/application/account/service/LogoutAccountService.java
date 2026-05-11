package com.furnisight.user.application.account.service;

import com.furnisight.user.application.account.dto.LogoutAccountCommand;
import com.furnisight.user.application.account.port.in.usecase.LogoutAccountUseCase;
import com.furnisight.user.domain.exceptions.identity.ErrorCode;
import com.furnisight.user.domain.exceptions.identity.NotFoundException;
import com.furnisight.user.domain.repository.identity.AccountTokenRepository;
import com.furnisight.user.domain.services.identity.account.AuthenticationService;
import com.furnisight.user.domain.valueobjects.identity.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogoutAccountService implements LogoutAccountUseCase {
    private final AuthenticationService authenticationService;
    private final AccountTokenRepository accountTokenRepository;

    @Override
    @Transactional
    public Void execute(LogoutAccountCommand logoutAccountCommand) {
        var token = accountTokenRepository.findByRefreshToken(new RefreshToken(logoutAccountCommand.refreshToken()))
            .orElseThrow(() -> new NotFoundException(ErrorCode.TOKEN_NOT_FOUND));
        authenticationService.logout(token);
        return null;
    }
}
