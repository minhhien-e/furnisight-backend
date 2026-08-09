package com.furnisight.user.domain.services.identity.account;

import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.entities.identity.AccountRole;
import com.furnisight.user.domain.entities.identity.Ban;
import com.furnisight.user.domain.entities.identity.Role;
import com.furnisight.user.domain.enums.identity.Permission;
import com.furnisight.user.domain.events.identity.AccountDeletedEvent;
import com.furnisight.user.domain.exceptions.identity.ErrorCode;
import com.furnisight.user.domain.exceptions.identity.InvalidOperationException;
import com.furnisight.user.domain.repository.identity.AccountRepository;
import com.furnisight.user.domain.repository.identity.AccountRoleRepository;
import com.furnisight.user.domain.repository.identity.BanRepository;
import com.furnisight.user.domain.repository.identity.RoleRepository;
import com.furnisight.user.domain.services.identity.token.TokenLifeCycleService;
import com.furnisight.user.domain.valueobjects.identity.BanReason;
import com.furnisight.user.domain.valueobjects.identity.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountModerationService {
    private final BanRepository banRepository;
    private final TokenLifeCycleService tokenLifeCycleService;
    private final RoleRepository roleRepository;
    private final AccountRepository accountRepository;
    private final AccountRoleRepository accountRoleRepository;
    private final AccountLifecycleService accountLifecycleService;

    public void activateAccount(Account admin, Account target) {
        ensureInteract(admin.getId(), target.getId(), Permission.ACCOUNT_MANAGE);

        target.activate();
    }

    public Ban banAccount(Account admin, Account target, BanReason reason, LocalDateTime expiresAt) {
        ensureInteract(admin.getId(), target.getId(), Permission.ACCOUNT_MANAGE);

        target.ban();
        tokenLifeCycleService.revokeAllAccountTokens(target);

        return banRepository.save(new Ban(target.getId(), reason, expiresAt));
    }

    public void unbanAccount(Account admin, Account target) {
        ensureInteract(admin.getId(), target.getId(), Permission.ACCOUNT_MANAGE);

        if (!target.isBanned()) {
            throw new InvalidOperationException(ErrorCode.ACCOUNT_NOT_BANNED);
        }

        target.activate();
        banRepository.disableAllByAccountId(target.getId());
    }

    public AccountRole assignRole(Account admin, Account target, UUID roleId) {
        ensureInteract(admin.getId(), target.getId(), Permission.ACCOUNT_MANAGE);
        return accountRoleRepository.findByAccountIdAndRoleId(target.getId(), roleId)
                .orElseGet(() -> accountRoleRepository.save(new AccountRole(target.getId(), roleId)));
    }

    public void revokeRole(Account admin, Account target, AccountRole accountRole) {
        ensureInteract(admin.getId(), target.getId(), Permission.ACCOUNT_MANAGE);
        accountRoleRepository.delete(accountRole);
    }

    public void deleteAccount(Account admin, Account target) {
        ensureInteract(admin.getId(), target.getId(), Permission.ACCOUNT_MANAGE);
        target.registerEvent(new AccountDeletedEvent(
                target.getId(),
                target.getEmail() != null ? target.getEmail().getValue() : null,
                LocalDateTime.now()));
        accountRepository.save(target);
        accountRepository.delete(target);

    }

    public Account provisionAccount(Account admin, String emailStr, String password, Role targetRole) {
        List<Role> adminRoles = roleRepository.findAllByAccountId(admin.getId());
        boolean hasManageUsers = adminRoles.stream()
                .anyMatch(role -> role.hasPermission(Permission.ACCOUNT_MANAGE));
        if (!hasManageUsers) {
            throw new InvalidOperationException(ErrorCode.NOT_ENOUGH_PERMISSION);
        }

        Email email = new Email(emailStr.trim());

        Account targetAccount = accountLifecycleService.provision(email, password);

        if (targetRole != null) {
            int adminHighestPosition = adminRoles.stream()
                    .mapToInt(Role::getPosition)
                    .min()
                    .orElse(Integer.MAX_VALUE);
            if (targetRole.getPosition() < adminHighestPosition) {
                throw new InvalidOperationException(ErrorCode.NOT_ENOUGH_PERMISSION);
            }
            accountRoleRepository.save(new AccountRole(targetAccount.getId(), targetRole.getId()));
        }

        return targetAccount;
    }
    public Account provisionAdminAccount(Account admin, String emailStr, String password, Role targetRole) {
        Account targetAccount = provisionAccount(admin, emailStr, password, targetRole);
        targetAccount.promoteToAdmin();
        return accountRepository.save(targetAccount);
    }
    private boolean canInteract(List<Role> rolesA, List<Role> rolesB, Permission permission) {
        boolean hasPermission = rolesA.stream().anyMatch(role -> role.hasPermission(permission));
        if (!hasPermission) {
            return false;
        }
        if (rolesB.isEmpty()) {
            return true;
        }
        return rolesA.stream()
                .filter(role -> role.hasPermission(permission))
                .anyMatch(roleA -> rolesB.stream().allMatch(
                        roleB -> roleA.getPosition() <= roleB.getPosition()));
    }

    private void ensureInteract(UUID accountIdA, UUID accountIdB, Permission permission) {
        List<Role> aRoles = roleRepository.findAllByAccountId(accountIdA);
        List<Role> bRoles = roleRepository.findAllByAccountId(accountIdB);
        if (!canInteract(aRoles, bRoles, permission))
            throw new InvalidOperationException(ErrorCode.NOT_ENOUGH_PERMISSION);
    }

}
