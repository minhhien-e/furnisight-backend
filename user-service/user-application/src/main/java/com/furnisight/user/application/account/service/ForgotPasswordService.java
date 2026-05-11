package com.furnisight.user.application.account.service;

import com.furnisight.user.application.account.dto.ForgotPasswordCommand;
import com.furnisight.user.application.account.port.in.usecase.ForgotPasswordUseCase;
import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.enums.identity.Channel;
import com.furnisight.user.domain.exceptions.identity.ErrorCode;
import com.furnisight.user.domain.exceptions.identity.NotFoundException;
import com.furnisight.user.domain.repository.identity.AccountRepository;
import com.furnisight.user.domain.services.identity.account.PasswordService;
import com.furnisight.user.domain.valueobjects.identity.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ForgotPasswordService implements ForgotPasswordUseCase {

    private final PasswordService passwordService;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public Void execute(ForgotPasswordCommand command) {
        Account account = null;
        if (Channel.EMAIL == Channel.valueOf(command.channel())) {
            Email email = new Email(command.destination());
            account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ACCOUNT_NOT_FOUND));
        }
        passwordService.requestResetPassword(account, command.channel());
        return null;
    }
}
