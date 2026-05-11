package com.furnisight.user.domain.services.identity.account;

import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.entities.identity.VerificationRequest;
import com.furnisight.user.domain.exceptions.identity.ErrorCode;
import com.furnisight.user.domain.exceptions.identity.ForbiddenException;
import com.furnisight.user.domain.services.identity.policy.PasswordPolicy;
import com.furnisight.user.domain.services.identity.token.TokenLifeCycleService;
import com.furnisight.user.domain.valueobjects.identity.Password;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordService {
    private final PasswordPolicy passwordPolicy;
    private final PasswordHasher passwordHasher;
    private final TokenLifeCycleService tokenLifeCycleService;
    private final AccountGuard accountGuard;

    public void changePassword(Account account, String newPassword) {
        passwordPolicy.validate(newPassword);
        Password hashedPassword = new Password(passwordHasher.hash(newPassword));
        account.setPassword(hashedPassword);

        tokenLifeCycleService.revokeAllAccountTokens(account);
    }

    public void requestResetPassword(Account account, String channel) {
        accountGuard.ensureVerifiedAndActive(account);

        tokenLifeCycleService.deletePasswordResetRequests(account);
        tokenLifeCycleService.sendPasswordResetOtp(account, channel);
    }

    /**
     * Step 1 of password reset: verify the OTP the user received.
     * inputCode is the raw code provided by the user (not token.getOtpCode()).
     */
    public void verifyResetPasswordCode(Account account, VerificationRequest token, String inputCode) {
        if (!token.getAccountId().equals(account.getId())) {
            throw new ForbiddenException(ErrorCode.TOKEN_NOT_BELONG_TO_ACCOUNT);
        }
        token.verifyOtp(inputCode);
    }

    /**
     * Step 2 of password reset: verify OTP again (or trust already verified) and apply new password.
     */
    public void resetPassword(Account account, VerificationRequest token, String inputCode, String newPassword) {
        if (!token.getAccountId().equals(account.getId())) {
            throw new ForbiddenException(ErrorCode.TOKEN_NOT_BELONG_TO_ACCOUNT);
        }

        token.verifyOtp(inputCode);
        token.complete();

        passwordPolicy.validate(newPassword);
        Password hashedPassword = new Password(passwordHasher.hash(newPassword));
        account.setPassword(hashedPassword);

        tokenLifeCycleService.revokeAllAccountTokens(account);
    }
}
