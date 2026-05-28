package com.furnisight.user.domain.services.identity.token;

import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.entities.identity.AccountToken;
import com.furnisight.user.domain.entities.identity.VerificationRequest;
import com.furnisight.user.domain.enums.identity.VerificationType;
import com.furnisight.user.domain.events.identity.AccountVerificationRequestedEvent;
import com.furnisight.user.domain.exceptions.identity.ErrorCode;
import com.furnisight.user.domain.exceptions.identity.UnauthorizedException;
import com.furnisight.user.domain.repository.identity.AccountTokenRepository;
import com.furnisight.user.domain.repository.identity.VerificationRequestRepository;
import com.furnisight.user.domain.services.identity.generator.AccessTokenGenerator;
import com.furnisight.user.domain.services.identity.generator.OtpCodeGenerator;
import com.furnisight.user.domain.services.identity.generator.RefreshTokenGenerator;
import com.furnisight.user.domain.valueobjects.identity.AccessToken;
import com.furnisight.user.domain.valueobjects.identity.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.furnisight.user.domain.repository.identity.RoleRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TokenLifeCycleService {

    private static final int OTP_TTL_MINUTES = 5;
    private static final int SESSION_TTL_MINUTES = 30;

    private final AccountTokenRepository accountTokenRepository;
    private final VerificationRequestRepository verificationRequestRepository;
    private final AccessTokenGenerator accessTokenGenerator;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final OtpCodeGenerator otpCodeGenerator;
    private final RoleRepository roleRepository;
    @Value("${app.security.verify-url:http://localhost:8080/users/auth/verify-email}")
    private String verifyUrl;

    // ─── JWT / Refresh tokens ──────────────────────────────────────────────────

    public AccountToken generateAccountToken(Account account) {
        AccessToken accessToken = accessTokenGenerator.generateToken(account);
        RefreshToken refreshToken = refreshTokenGenerator.generateToken();
        List<String> roles = roleRepository.findAllByAccountId(account.getId()).stream()
                .map(role -> role.getName().getValue()).toList();
        var accountToken = new AccountToken(account.getId(), accessToken, refreshToken, roles);
        return accountTokenRepository.save(accountToken);
    }

    public AccountToken renewAccessToken(Account account, AccountToken accountToken) {
        if (accountToken.refreshTokenIsExpired())
            throw new UnauthorizedException(ErrorCode.TOKEN_EXPIRED);
        accountToken.revoke();
        AccessToken newAccessToken = accessTokenGenerator.generateToken(account);
        RefreshToken refreshToken = refreshTokenGenerator.generateToken();
        List<String> roles = roleRepository.findAllByAccountId(account.getId()).stream()
                .map(role -> role.getName().getValue()).toList();
        var newAccountToken = new AccountToken(account.getId(), newAccessToken, refreshToken, roles);
        return accountTokenRepository.save(newAccountToken);
    }

    public void revokeAccountToken(AccountToken accountToken) {
        accountToken.revoke();
    }

    public void revokeAllAccountTokens(Account account) {
        List<AccountToken> tokens = accountTokenRepository.findAllByAccountId(account.getId());
        for (AccountToken token : tokens) {
            token.revoke();
        }
    }

    // ─── OTP / Verification requests ──────────────────────────────────────────
    public void sendAccountVerificationOtp(Account account, String channel) {
        String otp = otpCodeGenerator.generateOtpCode();
        VerificationRequest request = new VerificationRequest(
                account.getId(),
                VerificationType.ACCOUNT_VERIFICATION,
                channel, otp,
                LocalDateTime.now().plusMinutes(OTP_TTL_MINUTES),
                LocalDateTime.now().plusMinutes(SESSION_TTL_MINUTES));
        request.registerEvent(new AccountVerificationRequestedEvent(account.getId(), verifyUrl + request.getOtpCode(),
                LocalDateTime.now()));
        verificationRequestRepository.save(request);
    }

    public void sendPasswordResetOtp(Account account, String channel) {
        String otp = otpCodeGenerator.generateOtpCode();
        VerificationRequest request = new VerificationRequest(
                account.getId(),
                VerificationType.PASSWORD_RESET,
                channel, otp,
                LocalDateTime.now().plusMinutes(OTP_TTL_MINUTES),
                LocalDateTime.now().plusMinutes(SESSION_TTL_MINUTES));
        request.requestOtp(channel); // fires AccountResetPasswordRequestedEvent
        verificationRequestRepository.save(request);
    }

    public void deleteAccountVerificationRequests(Account account) {
        verificationRequestRepository.deleteByAccountIdAndType(account.getId(), VerificationType.ACCOUNT_VERIFICATION);
    }

    public void deletePasswordResetRequests(Account account) {
        verificationRequestRepository.deleteByAccountIdAndType(account.getId(), VerificationType.PASSWORD_RESET);
    }
}
