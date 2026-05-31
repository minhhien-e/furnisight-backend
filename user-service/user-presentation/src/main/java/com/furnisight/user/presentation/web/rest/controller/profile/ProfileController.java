package com.furnisight.user.presentation.web.rest.controller.profile;

import com.furnisight.user.application.common.port.in.CurrentUserProvider;
import com.furnisight.user.application.profile.dto.*;
import com.furnisight.user.application.profile.port.in.usecase.*;
import com.furnisight.user.domain.enums.identity.VerificationMethod;
import com.furnisight.user.domain.enums.identity.VerificationType;
import com.furnisight.user.presentation.web.rest.dto.request.profile.ContactChangeNewRequest;
import com.furnisight.user.presentation.web.rest.dto.request.profile.ContactChangeTypeRequest;
import com.furnisight.user.presentation.web.rest.dto.request.profile.ContactOtpRequest;
import com.furnisight.user.presentation.web.rest.dto.request.profile.UpdateProfileRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final GetProfileUseCase getProfileUseCase;
    private final UpdateProfileUseCase updateProfileUseCase;
    private final RequestContactChangeUseCase requestContactChangeUseCase;
    private final VerifyCurrentContactUseCase verifyCurrentContactUseCase;
    private final RequestNewContactUseCase requestNewContactUseCase;
    private final ConfirmContactChangeUseCase confirmContactChangeUseCase;
    private final RequestLinkContactUseCase requestLinkContactUseCase;
    private final ConfirmLinkContactUseCase confirmLinkContactUseCase;
    private final RemoveContactUseCase removeContactUseCase;
    private final CurrentUserProvider currentUserProvider;

    // ─── Standard profile ───────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<?> getProfile() {
        UUID accountId = currentUserProvider.getCurrentUserId();
        var profile = getProfileUseCase.execute(accountId);
        return ResponseEntity.ok(com.furnisight.user.presentation.web.rest.dto.response.ProfileResponse.from(profile));
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest request) {
        UUID accountId = currentUserProvider.getCurrentUserId();
        var command = new UpdateProfileCommand(
            accountId,
            request.displayName(),
            request.firstName(),
            request.lastName(),
            request.avatarUrl(),
            request.dateOfBirth(),
            request.gender()
        );
        var profile = updateProfileUseCase.execute(command);
        return ResponseEntity.ok(com.furnisight.user.presentation.web.rest.dto.response.ProfileResponse.from(profile));
    }

    // ─── Email / Phone change flow ───────────────────────────────────────────

    @PostMapping("/contact/change/request")
    public ResponseEntity<?> requestContactChange(@RequestBody ContactChangeTypeRequest request) {
        UUID accountId = currentUserProvider.getCurrentUserId();
        requestContactChangeUseCase.execute(new RequestContactChangeCommand(
            accountId, resolveVerificationType(request.type()),
            resolveVerificationMethod(request.verificationMethod())
        ));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/contact/change/verify-current")
    public ResponseEntity<?> verifyCurrentContact(@RequestBody ContactOtpRequest request) {
        UUID accountId = currentUserProvider.getCurrentUserId();
        verifyCurrentContactUseCase.execute(new VerifyCurrentContactCommand(
            accountId,
            resolveVerificationType(request.type()),
            request.otpCode()
        ));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/contact/change/request-new")
    public ResponseEntity<?> requestNewContact(@RequestBody ContactChangeNewRequest request) {
        UUID accountId = currentUserProvider.getCurrentUserId();
        requestNewContactUseCase.execute(new RequestNewContactCommand(
            accountId,
            resolveVerificationType(request.type()),
            request.newContact()
        ));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/contact/change/confirm")
    public ResponseEntity<?> confirmContactChange(@RequestBody ContactOtpRequest request) {
        UUID accountId = currentUserProvider.getCurrentUserId();
        confirmContactChangeUseCase.execute(new ConfirmContactChangeCommand(
            accountId,
            resolveVerificationType(request.type()),
            request.otpCode()
        ));
        return ResponseEntity.ok().build();
    }

    // ─── Link new contact flow (when absent) ─────────────────────────────────

    @PostMapping("/contact/link/request")
    public ResponseEntity<?> requestLinkContact(@RequestBody ContactChangeNewRequest request) {
        UUID accountId = currentUserProvider.getCurrentUserId();
        requestLinkContactUseCase.execute(new RequestLinkContactCommand(
            accountId,
            resolveVerificationLinkType(request.type()),
            request.newContact()
        ));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/contact/link/confirm")
    public ResponseEntity<?> confirmLinkContact(@RequestBody ContactOtpRequest request) {
        UUID accountId = currentUserProvider.getCurrentUserId();
        confirmLinkContactUseCase.execute(new ConfirmLinkContactCommand(
            accountId,
            resolveVerificationLinkType(request.type()),
            request.otpCode()
        ));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/contact/remove")
    public ResponseEntity<?> removeContact(@RequestBody ContactChangeTypeRequest request) {
        UUID accountId = currentUserProvider.getCurrentUserId();
        removeContactUseCase.execute(new RemoveContactCommand(accountId, request.type()));
        return ResponseEntity.ok().build();
    }

    private VerificationType resolveVerificationType(String typeStr) {
        if ("EMAIL".equalsIgnoreCase(typeStr)) return VerificationType.EMAIL_CHANGE;
        if ("PHONE".equalsIgnoreCase(typeStr)) return VerificationType.PHONE_CHANGE;
        throw new IllegalArgumentException("Invalid contact type: " + typeStr);
    }

    private VerificationType resolveVerificationLinkType(String typeStr) {
        if ("EMAIL".equalsIgnoreCase(typeStr)) return VerificationType.EMAIL_LINK;
        if ("PHONE".equalsIgnoreCase(typeStr)) return VerificationType.PHONE_LINK;
        throw new IllegalArgumentException("Invalid contact link type: " + typeStr);
    }

    private VerificationMethod resolveVerificationMethod(String typeStr) {
        if (typeStr == null || typeStr.isBlank()) return null;
        if ("EMAIL".equalsIgnoreCase(typeStr)) return VerificationMethod.EMAIL;
        if ("PHONE".equalsIgnoreCase(typeStr)) return VerificationMethod.PHONE;
        throw new IllegalArgumentException("Invalid contact type: " + typeStr);
    }

}
