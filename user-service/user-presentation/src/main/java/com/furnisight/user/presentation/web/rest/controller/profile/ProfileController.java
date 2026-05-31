package com.furnisight.user.presentation.web.rest.controller.profile;

import com.furnisight.user.application.common.port.in.CurrentUserProvider;
import com.furnisight.user.application.profile.dto.*;
import com.furnisight.user.application.profile.port.in.usecase.*;
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
            request.bio(),
            request.birthday(),
            request.gender()
        );
        var profile = updateProfileUseCase.execute(command);
        return ResponseEntity.ok(com.furnisight.user.presentation.web.rest.dto.response.ProfileResponse.from(profile));
    }

}
