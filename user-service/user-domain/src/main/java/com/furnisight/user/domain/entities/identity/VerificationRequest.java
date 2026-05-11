package com.furnisight.user.domain.entities.identity;

import com.furnisight.user.domain.enums.identity.VerificationStep;
import com.furnisight.user.domain.enums.identity.VerificationType;
import com.furnisight.user.domain.events.identity.AccountResetPasswordRequestedEvent;
import com.furnisight.user.domain.events.identity.AccountVerificationRequestedEvent;
import com.furnisight.user.domain.events.profile.EmailChangeOtpRequestedEvent;
import com.furnisight.user.domain.events.profile.PhoneChangeOtpRequestedEvent;
import com.furnisight.user.domain.events.profile.EmailLinkOtpRequestedEvent;
import com.furnisight.user.domain.events.profile.PhoneLinkOtpRequestedEvent;
import com.furnisight.user.domain.events.profile.VerifyCurrentEmailOtpRequestedEvent;
import com.furnisight.user.domain.events.profile.VerifyCurrentPhoneOtpRequestedEvent;
import com.furnisight.user.domain.exceptions.identity.ErrorCode;
import com.furnisight.user.domain.exceptions.identity.UnauthorizedException;
import com.furnisight.user.domain.exceptions.identity.ValidationException;
import com.furnisight.user.domain.seedwork.AggregateRoot;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A unified entity for all OTP/Token based flows:
 * - Account Verification (1 step)
 * - Password Reset (1 or 2 steps)
 * - Contact Change (2 steps)
 */
@Entity
@Table(name = "verification_requests")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VerificationRequest extends AggregateRoot {

    @Id
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VerificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VerificationStep step;

    @Column(name = "target_contact")
    private String targetContact; // email or phone for step 1

    @Column(name = "new_contact")
    private String newContact; // new email or phone for step 2 (change contact)

    @Column(name = "otp_code", length = 100)
    private String otpCode; // the code or token

    @Column(name = "otp_expires_at")
    private LocalDateTime otpExpiresAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    public VerificationRequest(UUID accountId, VerificationType type,
                               String targetContact, String otpCode,
                               LocalDateTime otpExpiresAt, LocalDateTime expiresAt) {
        this.id = UUID.randomUUID();
        this.accountId = accountId;
        this.type = type;
        this.step = VerificationStep.STEP_1_PENDING;
        this.targetContact = targetContact;
        this.otpCode = otpCode;
        this.otpExpiresAt = otpExpiresAt;
        this.expiresAt = expiresAt;
    }

    public void advanceToStep2(String newContact, String otpCode, LocalDateTime otpExpiresAt) {
        if (this.step != VerificationStep.STEP_1_PENDING) {
            throw new ValidationException(ErrorCode.INVALID_TOKEN_STATE);
        }
        this.step = VerificationStep.STEP_2_PENDING;
        this.newContact = newContact;
        this.otpCode = otpCode;
        this.otpExpiresAt = otpExpiresAt;
    }

    public void updateToStep2(String newContact, String otpCode, LocalDateTime otpExpiresAt) {
        if (this.step != VerificationStep.STEP_2_PENDING) {
            throw new ValidationException(ErrorCode.INVALID_TOKEN_STATE);
        }
        this.newContact = newContact;
        this.otpCode = otpCode;
        this.otpExpiresAt = otpExpiresAt;
    }

    public void verifyOtp(String inputCode) {
        if (isExpired()) throw new UnauthorizedException(ErrorCode.TOKEN_EXPIRED);
        if (LocalDateTime.now().isAfter(otpExpiresAt)) throw new UnauthorizedException(ErrorCode.TOKEN_EXPIRED);
        if (!this.otpCode.equals(inputCode)) throw new UnauthorizedException(ErrorCode.INVALID_TOKEN);
    }

    public void complete() {
        this.step = VerificationStep.COMPLETED;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isCompleted() {
        return this.step == VerificationStep.COMPLETED;
    }

    public void requestOtp(String destination) {
        switch (this.type) {
            case ACCOUNT_VERIFICATION -> registerEvent(new AccountVerificationRequestedEvent(
                accountId, otpCode, LocalDateTime.now()
            ));
            case PASSWORD_RESET -> registerEvent(new AccountResetPasswordRequestedEvent(
                accountId, otpCode, destination, LocalDateTime.now()
            ));
            // Step 1: xác minh contact hiện tại
            case EMAIL_CHANGE -> {
                if (this.step == VerificationStep.STEP_1_PENDING) {
                    registerEvent(new VerifyCurrentEmailOtpRequestedEvent(accountId, destination, otpCode, LocalDateTime.now()));
                } else {
                    registerEvent(new EmailChangeOtpRequestedEvent(accountId, destination, otpCode, LocalDateTime.now()));
                }
            }
            case PHONE_CHANGE -> {
                if (this.step == VerificationStep.STEP_1_PENDING) {
                    registerEvent(new VerifyCurrentPhoneOtpRequestedEvent(accountId, destination, otpCode, LocalDateTime.now()));
                } else {
                    registerEvent(new PhoneChangeOtpRequestedEvent(accountId, destination, otpCode, LocalDateTime.now()));
                }
            }
            case EMAIL_LINK -> registerEvent(new EmailLinkOtpRequestedEvent(accountId, destination, otpCode, LocalDateTime.now()));
            case PHONE_LINK -> registerEvent(new PhoneLinkOtpRequestedEvent(accountId, destination, otpCode, LocalDateTime.now()));
        }
    }
}
