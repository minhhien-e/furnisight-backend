package com.furnisight.user.application.account.service;

import com.furnisight.user.application.account.dto.RevokeRoleCommand;
import com.furnisight.user.application.account.port.in.usecase.RevokeRoleUseCase;
import com.furnisight.user.domain.entities.identity.AccountRole;
import com.furnisight.user.domain.exceptions.identity.ErrorCode;
import com.furnisight.user.domain.exceptions.identity.NotFoundException;
import com.furnisight.user.domain.repository.identity.AccountRoleRepository;
import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.repository.identity.AccountRepository;
import com.furnisight.user.domain.services.identity.account.AccountModerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RevokeRoleService implements RevokeRoleUseCase {

    private final AccountModerationService accountModerationService;
    private final AccountRoleRepository accountRoleRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public Void execute(RevokeRoleCommand command) {
        Account admin = accountRepository.findById(command.adminId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.ACCOUNT_NOT_FOUND));
        Account target = accountRepository.findById(command.targetAccountId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.ACCOUNT_NOT_FOUND));
                
        AccountRole accountRole = accountRoleRepository
                .findByAccountIdAndRoleId(command.targetAccountId(), command.roleId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.ROLE_NOT_FOUND));
                
        accountModerationService.revokeRole(admin, target, accountRole);
        return null;
    }
}
