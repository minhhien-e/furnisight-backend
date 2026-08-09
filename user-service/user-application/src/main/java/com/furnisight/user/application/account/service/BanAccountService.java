package com.furnisight.user.application.account.service;

import com.furnisight.user.application.account.dto.BanAccountCommand;
import com.furnisight.user.application.account.port.in.usecase.BanAccountUseCase;
import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.entities.identity.Ban;
import com.furnisight.user.domain.exceptions.identity.ErrorCode;
import com.furnisight.user.domain.exceptions.identity.NotFoundException;
import com.furnisight.user.domain.repository.identity.AccountRepository;
import com.furnisight.user.domain.services.identity.account.AccountModerationService;
import com.furnisight.user.domain.valueobjects.identity.BanReason;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BanAccountService implements BanAccountUseCase {

    private final AccountModerationService accountModerationService;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public Ban execute(BanAccountCommand command) {
        Account admin = accountRepository.findById(command.adminId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.ACCOUNT_NOT_FOUND));
        Account target = accountRepository.findById(command.targetAccountId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.ACCOUNT_NOT_FOUND));
        BanReason reason = new BanReason(command.reason());
        return accountModerationService.banAccount(admin, target, reason, command.expiresAt());
    }
}
