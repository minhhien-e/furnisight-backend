package com.furnisight.user.presentation.web.rest.dto.request.profile;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateProfileRequest(
        String displayName,
        String firstName,
        String lastName,
        UUID avatarMediaId,
        String bio,
        LocalDate birthday,
        String gender) {
}
