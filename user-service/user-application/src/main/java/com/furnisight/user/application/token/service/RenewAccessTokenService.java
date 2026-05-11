package com.furnisight.user.application.token.service;

import com.furnisight.user.application.token.dto.RenewAccessTokenCommand;
import com.furnisight.user.application.token.port.in.usecase.RenewAccessTokenUseCase;
import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.entities.identity.AccountToken;
import com.furnisight.user.domain.exceptions.identity.ErrorCode;
import com.furnisight.user.domain.exceptions.identity.NotFoundException;
import com.furnisight.user.domain.repository.identity.AccountRepository;
import com.furnisight.user.domain.repository.identity.AccountTokenRepository;
import com.furnisight.user.domain.services.identity.token.TokenLifeCycleService;
import com.furnisight.user.domain.valueobjects.identity.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RenewAccessTokenService implements RenewAccessTokenUseCase {
    private final TokenLifeCycleService tokenLifeCycleService;
    private final AccountTokenRepository accountTokenRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public AccountToken execute(RenewAccessTokenCommand command) {
        AccountToken accountToken = accountTokenRepository.findByRefreshToken(new RefreshToken(command.refreshToken()))
            .orElseThrow(() -> new NotFoundException(ErrorCode.TOKEN_NOT_FOUND));

        Account account = accountRepository.findById(accountToken.getAccountId())
            .orElseThrow(() -> new NotFoundException(ErrorCode.ACCOUNT_NOT_FOUND));

        return tokenLifeCycleService.renewAccessToken(account, accountToken);
    }
}
