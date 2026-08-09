package com.furnisight.user.application.account.service;

import com.furnisight.user.application.account.dto.DeleteOwnAccountCommand;
import com.furnisight.user.application.account.port.in.usecase.DeleteOwnAccountUseCase;
import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.exceptions.identity.ErrorCode;
import com.furnisight.user.domain.exceptions.identity.NotFoundException;
import com.furnisight.user.domain.repository.identity.AccountRepository;
import com.furnisight.user.domain.services.identity.account.AccountLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteOwnAccountService implements DeleteOwnAccountUseCase {

    private final AccountLifecycleService accountLifecycleService;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public Void execute(DeleteOwnAccountCommand command) {
        Account account = accountRepository.findById(command.accountId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.ACCOUNT_NOT_FOUND));
        accountLifecycleService.deleteAccount(account);
        return null;
    }
}
