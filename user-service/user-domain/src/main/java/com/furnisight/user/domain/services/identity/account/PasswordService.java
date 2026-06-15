package com.furnisight.user.domain.services.identity.account;

import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.enums.identity.VerificationType;
import com.furnisight.user.domain.exceptions.identity.ErrorCode;
import com.furnisight.user.domain.exceptions.identity.UnauthorizedException;
import com.furnisight.user.domain.repository.identity.OtpVerificationRepository;
import com.furnisight.user.domain.services.identity.generator.OtpHasher;
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
    private final OtpHasher otpHasher;
    private final OtpVerificationRepository otpVerificationRepository;

    public void changePassword(Account account, String currentPassword, String newPassword) {
        if (currentPassword == null
                || currentPassword.isBlank()
                || account.getPassword() == null
                || account.getPassword().getHash() == null
                || !passwordHasher.verify(currentPassword, account.getPassword().getHash())) {
            throw new UnauthorizedException(ErrorCode.INVALID_PASSWORD);
        }
        passwordPolicy.validate(newPassword);
        Password hashedPassword = new Password(passwordHasher.hash(newPassword));
        account.setPassword(hashedPassword);

        tokenLifeCycleService.revokeAllAccountTokens(account);
    }

    public void requestResetPassword(Account account, String channel) {
        accountGuard.ensureVerifiedAndActive(account);
        tokenLifeCycleService.sendPasswordResetOtp(account, channel);
    }

    /**
     * Step 1 of password reset: verify the OTP the user received.
     * inputCode is the raw code provided by the user.
     */
    public void verifyResetPasswordCode(Account account, String otpHash, String inputCode) {
        verifyOtpHash(otpHash, inputCode);
    }

    /**
     * Step 2 of password reset: verify OTP again (or trust already verified) and apply new password.
     */
    public void resetPassword(Account account, String otpHash, String inputCode, String newPassword) {
        verifyOtpHash(otpHash, inputCode);

        passwordPolicy.validate(newPassword);
        Password hashedPassword = new Password(passwordHasher.hash(newPassword));
        account.setPassword(hashedPassword);

        tokenLifeCycleService.revokeAllAccountTokens(account);
        otpVerificationRepository.deleteByHashAndType(otpHash, VerificationType.PASSWORD_RESET);
    }

    private void verifyOtpHash(String expectedHash, String inputCode) {
        if (!expectedHash.equals(otpHasher.hash(inputCode))) {
            throw new UnauthorizedException(ErrorCode.INVALID_TOKEN);
        }
    }
}
