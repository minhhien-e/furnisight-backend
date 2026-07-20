package com.furnisight.user.application.account.service;

import com.furnisight.user.application.account.dto.LoginAccountCommand;
import com.furnisight.user.application.account.port.in.usecase.LoginAccountUseCase;
import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.exceptions.DomainException;
import com.furnisight.user.domain.exceptions.identity.ErrorCode;
import com.furnisight.user.domain.exceptions.identity.NotFoundException;
import com.furnisight.user.domain.entities.identity.AccountToken;
import com.furnisight.user.domain.repository.identity.AccountRepository;
import com.furnisight.user.domain.services.identity.account.AuthenticationService;
import com.furnisight.user.domain.services.identity.token.TokenLifeCycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginAccountService implements LoginAccountUseCase {

    private final AuthenticationService authenticationService;
    private final TokenLifeCycleService tokenLifeCycleService;
    private final AccountRepository accountRepository;

    @Override
    public AccountToken execute(LoginAccountCommand command) {
        String identifier = command.identifier() == null ? "" : command.identifier().trim();
        Account account = accountRepository.findByCredential(identifier)
            .orElseThrow(() -> new NotFoundException(ErrorCode.ACCOUNT_NOT_FOUND));
        boolean isValidPassword = authenticationService.login(account, command.password());
        accountRepository.save(account);
        
        if (!isValidPassword)
            throw new DomainException(ErrorCode.INVALID_PASSWORD);
            
        return tokenLifeCycleService.generateAccountToken(account);
    }
}
