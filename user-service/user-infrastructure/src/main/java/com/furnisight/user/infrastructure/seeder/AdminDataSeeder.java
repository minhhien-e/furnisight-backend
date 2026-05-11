package com.furnisight.user.infrastructure.seeder;

import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.entities.identity.AccountRole;
import com.furnisight.user.domain.entities.identity.Role;
import com.furnisight.user.domain.enums.identity.Permission;
import com.furnisight.user.domain.repository.identity.AccountRepository;
import com.furnisight.user.domain.repository.identity.AccountRoleRepository;
import com.furnisight.user.domain.repository.identity.RoleRepository;
import com.furnisight.user.domain.services.identity.account.PasswordHasher;
import com.furnisight.user.domain.valueobjects.identity.Email;
import com.furnisight.user.domain.valueobjects.identity.Password;
import com.furnisight.user.domain.valueobjects.identity.RoleName;
import com.furnisight.user.domain.valueobjects.identity.Username;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminDataSeeder implements ApplicationRunner {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_EMAIL    = "admin@furnisight.com";
    private static final String ADMIN_PASSWORD  = "admin";
    private static final String ADMIN_ROLE_NAME = "ADMIN";

    private final AccountRepository     accountRepository;
    private final RoleRepository        roleRepository;
    private final AccountRoleRepository accountRoleRepository;
    private final PasswordHasher        passwordHasher;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Role adminRole = seedAdminRole();
        seedAdminAccount(adminRole);
    }

    private Role seedAdminRole() {
        RoleName roleName = new RoleName(ADMIN_ROLE_NAME);
        return roleRepository.findByName(roleName).orElseGet(() -> {
            log.info("[Seeder] Creating ADMIN role with all permissions...");
            Role role = new Role(roleName, 0);
            for (Permission perm : Permission.values()) {
                role.grantPermission(perm);
            }
            Role saved = roleRepository.save(role);
            log.info("[Seeder] ADMIN role created: id={}", saved.getId());
            return saved;
        });
    }

    private void seedAdminAccount(Role adminRole) {
        Username username = new Username(ADMIN_USERNAME);
        accountRepository.findByUsername(username).ifPresentOrElse(
                existing -> log.info("[Seeder] Admin account already exists, skipping."),
                () -> {
                    log.info("[Seeder] Creating admin account...");
                    String hashedPassword = passwordHasher.hash(ADMIN_PASSWORD);
                    Account account = new Account(
                            username,
                            new Email(ADMIN_EMAIL),
                            new Password(hashedPassword)
                    );
                    account.activate(); // set ACTIVE immediately — no email verification needed
                    Account saved = accountRepository.save(account);

                    // Assign ADMIN role
                    accountRoleRepository.findByAccountIdAndRoleId(saved.getId(), adminRole.getId())
                            .ifPresentOrElse(
                                    r -> log.info("[Seeder] Admin role already assigned."),
                                    () -> {
                                        accountRoleRepository.save(new AccountRole(saved.getId(), adminRole.getId()));
                                        log.info("[Seeder] Admin account created: id={}", saved.getId());
                                    }
                            );
                }
        );
    }
}
