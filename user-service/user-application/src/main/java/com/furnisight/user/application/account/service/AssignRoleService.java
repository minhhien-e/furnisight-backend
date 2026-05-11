package com.furnisight.user.application.account.service;

import com.furnisight.user.application.account.dto.AssignRoleCommand;
import com.furnisight.user.application.account.port.in.usecase.AssignRoleUseCase;
import com.furnisight.user.domain.entities.identity.AccountRole;
import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.exceptions.identity.ErrorCode;
import com.furnisight.user.domain.exceptions.identity.NotFoundException;
import com.furnisight.user.domain.repository.identity.AccountRepository;
import com.furnisight.user.domain.services.identity.account.AccountModerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssignRoleService implements AssignRoleUseCase {

    private final AccountModerationService accountModerationService;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public AccountRole execute(AssignRoleCommand command) {
        Account admin = accountRepository.findById(command.adminId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.ACCOUNT_NOT_FOUND));
        Account target = accountRepository.findById(command.targetAccountId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.ACCOUNT_NOT_FOUND));
                
        return accountModerationService.assignRole(admin, target, command.roleId());
    }
}
