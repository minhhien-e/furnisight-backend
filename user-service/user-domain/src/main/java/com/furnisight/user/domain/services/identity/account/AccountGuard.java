package com.furnisight.user.domain.services.identity.account;

import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.exceptions.identity.ErrorCode;
import com.furnisight.user.domain.exceptions.identity.ForbiddenException;
import org.springframework.stereotype.Service;

@Service
public class AccountGuard {
    public void ensureVerifiedAndActive(Account account) {
        ensureVerified(account);
        ensureActive(account);
    }

    public void ensureVerified(Account account) {
        if (!account.isVerified()) {
            throw new ForbiddenException(ErrorCode.ACCOUNT_NOT_VERIFIED);
        }
    }

    public void ensureActive(Account account) {
        if (account.isBanned()) {
            throw new ForbiddenException(ErrorCode.ACCOUNT_BANNED);
        }
        if (account.isLocked()) {
            throw new ForbiddenException(ErrorCode.ACCOUNT_TEMPORARILY_LOCKED);
        }
    }

}
