package com.furnisight.user.application.account.service;

import com.furnisight.user.application.account.dto.RegisterAccountCommand;
import com.furnisight.user.application.account.port.in.usecase.RegisterAccountUseCase;
import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.entities.identity.AccountToken;
import com.furnisight.user.domain.services.identity.account.AccountLifecycleService;
import com.furnisight.user.domain.services.profile.UserProfileLifecycleService;
import com.furnisight.user.domain.services.identity.token.TokenLifeCycleService;
import com.furnisight.user.domain.valueobjects.identity.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterAccountService implements RegisterAccountUseCase {

    private final AccountLifecycleService accountLifecycleService;
    private final UserProfileLifecycleService userProfileLifecycleService;
    private final TokenLifeCycleService tokenLifeCycleService;

    @Override
    @Transactional
    public AccountToken execute(RegisterAccountCommand command) {
        Email email = new Email(command.email());
        Account account = accountLifecycleService.register(email, command.password());
        
        String fullName = command.fullName() == null ? "" : command.fullName().trim();

        userProfileLifecycleService.createProfile(
                account.getId(),
                fullName,
                command.email());
        return tokenLifeCycleService.generateAccountToken(account);
    }
}
