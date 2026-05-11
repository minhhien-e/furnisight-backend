package com.furnisight.user.domain.services.identity.account;

import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.entities.identity.AccountRole;
import com.furnisight.user.domain.entities.identity.Ban;
import com.furnisight.user.domain.entities.identity.Role;
import com.furnisight.user.domain.enums.identity.Permission;
import com.furnisight.user.domain.exceptions.identity.ErrorCode;
import com.furnisight.user.domain.exceptions.identity.InvalidOperationException;
import com.furnisight.user.domain.repository.identity.AccountRepository;
import com.furnisight.user.domain.repository.identity.AccountRoleRepository;
import com.furnisight.user.domain.repository.identity.BanRepository;
import com.furnisight.user.domain.repository.identity.RoleRepository;
import com.furnisight.user.domain.services.identity.token.TokenLifeCycleService;
import com.furnisight.user.domain.valueobjects.identity.BanReason;
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

    public void activateAccount(Account admin, Account target) {
        ensureInteract(admin.getId(), target.getId(), Permission.MANAGE_USERS);

        target.activate();
    }

    public Ban banAccount(Account admin, Account target, BanReason reason, LocalDateTime expiresAt) {
        ensureInteract(admin.getId(), target.getId(), Permission.MANAGE_BANS);

        target.ban();
        tokenLifeCycleService.revokeAllAccountTokens(target);

        return banRepository.save(new Ban(target.getId(), reason, expiresAt));
    }

    public void unbanAccount(Account admin, Account target) {
        ensureInteract(admin.getId(), target.getId(), Permission.MANAGE_BANS);

        if (!target.isBanned()) {
            throw new InvalidOperationException(ErrorCode.ACCOUNT_NOT_BANNED);
        }

        target.activate();
        banRepository.disableAllByAccountId(target.getId());
    }

    public AccountRole assignRole(Account admin, Account target, UUID roleId) {
        ensureInteract(admin.getId(), target.getId(), Permission.MANAGE_ROLES);
        return accountRoleRepository.save(new AccountRole(target.getId(), roleId));
    }

    public void revokeRole(Account admin, Account target, AccountRole accountRole) {
        ensureInteract(admin.getId(), target.getId(), Permission.MANAGE_ROLES);
        accountRoleRepository.delete(accountRole);
    }

    public void deleteAccount(Account admin, Account target) {
        ensureInteract(admin.getId(), target.getId(), Permission.MANAGE_USERS);
        accountRepository.delete(target);

    }

    private boolean canInteract(List<Role> rolesA, List<Role> rolesB, Permission permission) {
        return rolesA.stream().anyMatch(
            roleA -> roleA.hasPermission(permission) && rolesB.stream().anyMatch(
                roleB -> roleA.getPosition() >= roleB.getPosition()
            )
        );
    }

    private void ensureInteract(UUID accountIdA, UUID accountIdB, Permission permission) {
        List<Role> aRoles = roleRepository.findAllByAccountId(accountIdA);
        List<Role> bRoles = roleRepository.findAllByAccountId(accountIdB);
        if (!canInteract(aRoles, bRoles, permission))
            throw new InvalidOperationException(ErrorCode.NOT_ENOUGH_PERMISSION);
    }

}
