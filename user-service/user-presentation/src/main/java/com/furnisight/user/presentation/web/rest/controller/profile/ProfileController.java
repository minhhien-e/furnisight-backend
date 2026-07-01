package com.furnisight.user.presentation.web.rest.controller.profile;

import com.furnisight.user.application.common.port.in.CurrentUserProvider;
import com.furnisight.user.application.profile.dto.*;
import com.furnisight.user.application.profile.port.in.usecase.*;
import com.furnisight.user.presentation.web.rest.dto.request.profile.UpdateProfileRequest;
import com.furnisight.user.presentation.web.rest.dto.response.ProfileResponse;
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
        ProfileResult result = getProfileUseCase.execute(accountId);
        return ResponseEntity.ok(ProfileResponse.from(result.profile(), result.avatarUrl()));
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest request) {
        UUID accountId = currentUserProvider.getCurrentUserId();
        var command = new UpdateProfileCommand(
            accountId,
            request.displayName(),
            request.fullName(),
            request.avatarMediaId(),
            request.bio(),
            request.birthday(),
            request.gender()
        );
        ProfileResult result = updateProfileUseCase.execute(command);
        return ResponseEntity.ok(ProfileResponse.from(result.profile(), result.avatarUrl()));
    }
}
