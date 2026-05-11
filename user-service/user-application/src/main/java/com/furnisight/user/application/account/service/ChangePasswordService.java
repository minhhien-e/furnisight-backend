package com.furnisight.user.application.account.service;

import com.furnisight.user.application.account.dto.ChangePasswordCommand;
import com.furnisight.user.application.account.port.in.usecase.ChangePasswordUseCase;
import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.exceptions.identity.ErrorCode;
import com.furnisight.user.domain.exceptions.identity.NotFoundException;
import com.furnisight.user.domain.repository.identity.AccountRepository;
import com.furnisight.user.domain.services.identity.account.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChangePasswordService implements ChangePasswordUseCase {

    private final PasswordService passwordService;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public Void execute(ChangePasswordCommand command) {
        Account account = accountRepository.findById(command.accountId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.ACCOUNT_NOT_FOUND));
        passwordService.changePassword(account, command.newPassword());
        return null;
    }
}
