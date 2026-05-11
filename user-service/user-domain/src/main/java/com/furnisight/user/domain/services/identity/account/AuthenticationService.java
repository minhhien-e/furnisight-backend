package com.furnisight.user.domain.services.identity.account;

import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.entities.identity.AccountToken;
import com.furnisight.user.domain.services.identity.token.TokenLifeCycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final PasswordHasher passwordHasher;
    private final TokenLifeCycleService tokenLifeCycleService;
    private final AccountGuard accountGuard;

    public boolean login(Account account, String password) {
        accountGuard.ensureVerifiedAndActive(account);
        boolean isValid = passwordHasher.verify(password, account.getPassword().getHash());

        if (!isValid) {
            account.incrementFailedLoginAttempts();
            if (account.getFailedLoginAttempts() >= 5) {
                account.lockTemporarily(LocalDateTime.now().plusMinutes(15));
            }
            return false;
        }

        account.resetFailedLoginAttempts();
        return true;
    }

    public void logout(AccountToken token) {
        tokenLifeCycleService.revokeAccountToken(token);
    }

    public void logoutAll(Account account) {
        tokenLifeCycleService.revokeAllAccountTokens(account);
    }
}
