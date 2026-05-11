package com.furnisight.user.domain.services.identity.account;

import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.entities.identity.VerificationRequest;
import com.furnisight.user.domain.exceptions.identity.ErrorCode;
import com.furnisight.user.domain.exceptions.identity.ForbiddenException;
import com.furnisight.user.domain.services.identity.token.TokenLifeCycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerificationService {
    private final TokenLifeCycleService tokenLifeCycleService;

    public void verifyAccount(Account account, VerificationRequest token, String inputCode) {
        if (!token.getAccountId().equals(account.getId())) {
            throw new ForbiddenException(ErrorCode.TOKEN_NOT_BELONG_TO_ACCOUNT);
        }

        token.verifyOtp(inputCode);
        token.complete();
        account.activate();
    }

    public void requestVerification(Account account, String channel) {
        tokenLifeCycleService.deleteAccountVerificationRequests(account);
        tokenLifeCycleService.sendAccountVerificationOtp(account, channel);
    }
}
