package com.furnisight.user.presentation.web.rest.dto.request.profile;

import java.time.LocalDate;

public record UpdateProfileRequest(
        String displayName,
        String firstName,
        String lastName,
        String avatarUrl,
        String bio,
        LocalDate birthday,
        String gender) {
}
