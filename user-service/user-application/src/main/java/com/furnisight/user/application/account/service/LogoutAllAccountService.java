package com.furnisight.user.application.account.service;

import com.furnisight.user.application.account.dto.LogoutAllAccountCommand;
import com.furnisight.user.application.account.port.in.usecase.LogoutAllAccountUseCase;
import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.exceptions.identity.ErrorCode;
import com.furnisight.user.domain.exceptions.identity.NotFoundException;
import com.furnisight.user.domain.repository.identity.AccountRepository;
import com.furnisight.user.domain.services.identity.account.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogoutAllAccountService implements LogoutAllAccountUseCase {

    private final AuthenticationService authenticationService;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public Void execute(LogoutAllAccountCommand command) {
        Account account = accountRepository.findById(command.accountId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.ACCOUNT_NOT_FOUND));
        authenticationService.logoutAll(account);
        return null;
    }
}
