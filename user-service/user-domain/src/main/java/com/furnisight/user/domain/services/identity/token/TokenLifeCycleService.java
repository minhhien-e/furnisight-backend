package com.furnisight.user.domain.services.identity.token;

import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.entities.identity.AccountToken;
import com.furnisight.user.domain.enums.identity.VerificationType;
import com.furnisight.user.domain.events.identity.AccountResetPasswordRequestedEvent;
import com.furnisight.user.domain.events.identity.AccountVerificationRequestedEvent;
import com.furnisight.user.domain.exceptions.identity.ErrorCode;
import com.furnisight.user.domain.exceptions.identity.UnauthorizedException;
import com.furnisight.user.domain.repository.identity.AccountTokenRepository;
import com.furnisight.user.domain.repository.identity.OtpVerificationRepository;
import com.furnisight.user.domain.services.identity.generator.AccessTokenGenerator;
import com.furnisight.user.domain.services.identity.generator.OtpCodeGenerator;
import com.furnisight.user.domain.services.identity.generator.OtpHasher;
import com.furnisight.user.domain.services.identity.generator.RefreshTokenGenerator;
import com.furnisight.user.domain.valueobjects.identity.AccessToken;
import com.furnisight.user.domain.valueobjects.identity.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.furnisight.user.domain.repository.identity.RoleRepository;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TokenLifeCycleService {

    private static final int OTP_TTL_MINUTES = 5;

    private final AccountTokenRepository accountTokenRepository;
    private final OtpVerificationRepository otpVerificationRepository;
    private final AccessTokenGenerator accessTokenGenerator;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final OtpCodeGenerator otpCodeGenerator;
    private final OtpHasher otpHasher;
    private final RoleRepository roleRepository;
    private final ApplicationEventPublisher eventPublisher;
    @Value("${app.verify-url:http://localhost:8080/users/auth/verify?otpCode=}")
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
    public void sendAccountVerificationOtp(Account account, String email) {
        String otp = otpCodeGenerator.generateOtpCode();
        otpVerificationRepository.save(
                email,
                VerificationType.ACCOUNT_VERIFICATION,
                otpHasher.hash(otp),
                Duration.ofMinutes(OTP_TTL_MINUTES));
        eventPublisher.publishEvent(new AccountVerificationRequestedEvent(
                account.getId(),
                email,
                verifyUrl + otp,
                java.time.LocalDateTime.now()));
    }

    public void sendPasswordResetOtp(Account account, String email) {
        String otp = otpCodeGenerator.generateOtpCode();
        otpVerificationRepository.save(
                email,
                VerificationType.PASSWORD_RESET,
                otpHasher.hash(otp),
                Duration.ofMinutes(OTP_TTL_MINUTES));
        eventPublisher.publishEvent(
                new AccountResetPasswordRequestedEvent(account.getId(), otp, email, java.time.LocalDateTime.now()));
    }

}
