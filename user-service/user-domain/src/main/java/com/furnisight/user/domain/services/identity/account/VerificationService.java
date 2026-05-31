package com.furnisight.user.domain.services.identity.account;

import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.enums.identity.VerificationType;
import com.furnisight.user.domain.exceptions.identity.ErrorCode;
import com.furnisight.user.domain.exceptions.identity.UnauthorizedException;
import com.furnisight.user.domain.repository.identity.OtpVerificationRepository;
import com.furnisight.user.domain.services.identity.generator.OtpHasher;
import com.furnisight.user.domain.services.identity.token.TokenLifeCycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerificationService {
    private final TokenLifeCycleService tokenLifeCycleService;
    private final OtpHasher otpHasher;
    private final OtpVerificationRepository otpVerificationRepository;

    public void verifyAccount(Account account, String otpHash, String inputCode) {
        verifyOtpHash(otpHash, inputCode);
        account.activate();
        otpVerificationRepository.deleteByHashAndType(otpHash, VerificationType.ACCOUNT_VERIFICATION);
    }

    public void requestVerification(Account account, String channel) {
        tokenLifeCycleService.sendAccountVerificationOtp(account, channel);
    }

    private void verifyOtpHash(String expectedHash, String inputCode) {
        if (!expectedHash.equals(otpHasher.hash(inputCode))) {
            throw new UnauthorizedException(ErrorCode.INVALID_TOKEN);
        }
    }
}
