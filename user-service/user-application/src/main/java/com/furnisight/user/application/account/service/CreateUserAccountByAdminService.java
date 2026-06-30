package com.furnisight.user.application.account.service;

import com.furnisight.user.application.account.dto.CreateAccountCommand;
import com.furnisight.user.application.account.port.in.usecase.CreateUserAccountByAdminUseCase;
import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.entities.identity.Role;
import com.furnisight.user.domain.exceptions.identity.ErrorCode;
import com.furnisight.user.domain.exceptions.identity.NotFoundException;
import com.furnisight.user.domain.repository.identity.AccountRepository;
import com.furnisight.user.domain.repository.identity.RoleRepository;
import com.furnisight.user.domain.services.identity.account.AccountModerationService;
import com.furnisight.user.domain.services.profile.UserProfileLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateUserAccountByAdminService implements CreateUserAccountByAdminUseCase {

    private final UserProfileLifecycleService userProfileLifecycleService;
    private final AccountModerationService accountModerationService;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public Void execute(CreateAccountCommand command) {
        // 1. Resolve admin
        Account admin = accountRepository.findById(command.adminId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.ACCOUNT_NOT_FOUND));

        // 2. Resolve target role if specified
        Role targetRole = null;
        if (command.roleId() != null) {
            targetRole = roleRepository.findById(command.roleId())
                    .orElseThrow(() -> new NotFoundException(ErrorCode.ROLE_NOT_FOUND));
        }

        // 3. Delegate business rules & creation to domain service
        Account targetAccount = accountModerationService.provisionAccount(
                admin,
                command.email(),
                command.password(),
                targetRole
        );

        // 4. Create target user profile
        String fullName = command.fullName() == null ? "" : command.fullName().trim();
        String firstName = "";
        String lastName = "";
        if (!fullName.isEmpty()) {
            int firstSpace = fullName.indexOf(' ');
            if (firstSpace == -1) {
                lastName = fullName;
            } else {
                lastName = fullName.substring(0, firstSpace);
                firstName = fullName.substring(firstSpace + 1).trim();
            }
        }

        userProfileLifecycleService.createProfile(
                targetAccount.getId(),
                firstName,
                lastName,
                command.email());

        return null;
    }
}
