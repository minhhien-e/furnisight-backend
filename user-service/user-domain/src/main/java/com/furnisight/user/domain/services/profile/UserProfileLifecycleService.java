package com.furnisight.user.domain.services.profile;

import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.entities.identity.VerificationRequest;
import com.furnisight.user.domain.entities.profile.UserProfile;
import com.furnisight.user.domain.enums.identity.VerificationStep;
import com.furnisight.user.domain.enums.identity.VerificationType;
import com.furnisight.user.domain.enums.identity.VerificationMethod;
import com.furnisight.user.domain.enums.profile.Gender;
import com.furnisight.user.domain.exceptions.identity.ErrorCode;
import com.furnisight.user.domain.exceptions.identity.NotFoundException;
import com.furnisight.user.domain.exceptions.identity.ValidationException;
import com.furnisight.user.domain.repository.identity.VerificationRequestRepository;
import com.furnisight.user.domain.repository.profile.UserProfileRepository;
import com.furnisight.user.domain.services.identity.generator.OtpCodeGenerator;
import com.furnisight.user.domain.valueobjects.identity.Email;
import com.furnisight.user.domain.valueobjects.profile.PhoneNumber;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileLifecycleService {

    private final UserProfileRepository userProfileRepository;
    private final VerificationRequestRepository verificationRequestRepository;
    private final OtpCodeGenerator otpCodeGenerator;
    private static final int OTP_TTL_MINUTES = 5;
    private static final int SESSION_TTL_MINUTES = 30;

    public UserProfile createProfile(UUID accountId, String firstName, String lastName,
                                     String email) {
        Email emailVO = email != null ? new Email(email) : null;
        UserProfile newProfile = new UserProfile(accountId, firstName, lastName, emailVO, null);
        return userProfileRepository.save(newProfile);
    }

    public UserProfile getProfile(UUID accountId) {
        return userProfileRepository.findByAccountId(accountId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.PROFILE_NOT_FOUND));
    }

    public UserProfile updateProfile(UserProfile profile, String displayName, String firstName, String lastName,
                                     String avatarUrl,
                                     LocalDate dateOfBirth, String gender) {
        Gender genderEnum = (gender != null && !gender.isBlank())
            ? Gender.valueOf(gender.toUpperCase())
            : null;
        profile.updateProfile(displayName, firstName, lastName, avatarUrl, dateOfBirth, genderEnum);
        return profile;
    }

    public VerificationRequest initiateContactChange(
        Account account,
        UserProfile profile,
        VerificationType type,
        VerificationMethod verifyBy) {
        // Vô hiệu hóa các otp trước đó
        verificationRequestRepository.deleteByAccountIdAndType(account.getId(), type);

        String currentContact = resolveCurrentContact(type, verifyBy, account, profile);
        if (currentContact == null || currentContact.isBlank()) {
            throw new ValidationException(ErrorCode.CONTACT_NOT_FOUND);
        }

        String otp = otpCodeGenerator.generateOtpCode();

        VerificationRequest request = new VerificationRequest(
            account.getId(), type, currentContact, otp,
            LocalDateTime.now().plusMinutes(OTP_TTL_MINUTES),
            LocalDateTime.now().plusMinutes(SESSION_TTL_MINUTES)
        );
        request.requestOtp(currentContact);
        return verificationRequestRepository.save(request);
    }

    public void verifyCurrentContact(VerificationRequest request, String inputCode) {
        if (request.getStep() != VerificationStep.STEP_1_PENDING) {
            throw new ValidationException(ErrorCode.INVALID_TOKEN_STATE);
        }
        request.verifyOtp(inputCode);
        request.advanceToStep2(null, null, null);

    }

    public void submitNewContact(
        VerificationRequest request,
        String newContact) {
        String otp = otpCodeGenerator.generateOtpCode();
        LocalDateTime otpExpiresAt = LocalDateTime.now().plusMinutes(OTP_TTL_MINUTES);
        request.updateToStep2(newContact, otp, otpExpiresAt);
        request.requestOtp(newContact);
        verificationRequestRepository.save(request);
    }

    public void applyContactChange(
        Account account,
        UserProfile profile,
        VerificationRequest request,
        String inputCode) {

        if (request.getStep() != VerificationStep.STEP_2_PENDING) {
            throw new ValidationException(ErrorCode.INVALID_TOKEN_STATE);
        }

        request.verifyOtp(inputCode);
        request.complete();

        String newContact = request.getNewContact();

        if (request.getType() == VerificationType.EMAIL_CHANGE) {
            Email newEmail = new Email(newContact);
            account.changeEmail(newEmail);
            profile.setEmail(newEmail);
        } else {
            profile.setPhoneNumber(new PhoneNumber(newContact));
        }
    }

    public VerificationRequest initiateContactLink(
        Account account,
        VerificationType type,
        String newContact) {
        
        // Vô hiệu hóa các otp trước đó
        verificationRequestRepository.deleteByAccountIdAndType(account.getId(), type);

        String otp = otpCodeGenerator.generateOtpCode();

        VerificationRequest request = new VerificationRequest(
            account.getId(), type, newContact, otp,
            LocalDateTime.now().plusMinutes(OTP_TTL_MINUTES),
            LocalDateTime.now().plusMinutes(SESSION_TTL_MINUTES)
        );
        request.requestOtp(newContact);
        return verificationRequestRepository.save(request);
    }

    public void applyContactLink(
        Account account,
        UserProfile profile,
        VerificationRequest request,
        String inputCode) {

        if (request.getStep() != VerificationStep.STEP_1_PENDING) {
            throw new ValidationException(ErrorCode.INVALID_TOKEN_STATE);
        }

        request.verifyOtp(inputCode);
        request.complete();

        String newContact = request.getTargetContact();

        if (request.getType() == VerificationType.EMAIL_LINK) {
            Email newEmail = new Email(newContact);
            account.changeEmail(newEmail);
            profile.setEmail(newEmail);
        } else if (request.getType() == VerificationType.PHONE_LINK) {
            profile.setPhoneNumber(new PhoneNumber(newContact));
        }
    }

    private String resolveCurrentContact(VerificationType type, VerificationMethod verifyBy, Account account, UserProfile profile) {
        if (verifyBy != null) {
            if (verifyBy == VerificationMethod.EMAIL) {
                return account.getEmail() != null ? account.getEmail().getValue() : null;
            } else if (verifyBy == VerificationMethod.PHONE) {
                return profile.getPhoneNumber() != null ? profile.getPhoneNumber().getValue() : null;
            }
        }
        if (type == VerificationType.EMAIL_CHANGE) {
            return account.getEmail() != null ? account.getEmail().getValue() : null;
        }
        return profile.getPhoneNumber() != null ? profile.getPhoneNumber().getValue() : null;
    }
}
