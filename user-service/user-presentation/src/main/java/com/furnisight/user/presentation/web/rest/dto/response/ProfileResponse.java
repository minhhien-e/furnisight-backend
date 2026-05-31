package com.furnisight.user.presentation.web.rest.dto.response;

import com.furnisight.user.domain.entities.profile.UserProfile;

import java.time.LocalDate;
import java.util.UUID;

public record ProfileResponse(
    UUID id,
    UUID accountId,
    String displayName,
    String firstName,
    String lastName,
    String avatarUrl,
    String email,
    String bio,
    LocalDate birthday,
    String gender
) {
    public static ProfileResponse from(UserProfile profile, String avatarUrl) {
        return new ProfileResponse(
            profile.getId(),
            profile.getAccountId(),
            profile.getDisplayName(),
            profile.getFirstName(),
            profile.getLastName(),
            avatarUrl,
            profile.getEmail() != null ? profile.getEmail().getValue() : null,
            profile.getBio(),
            profile.getDateOfBirth(),
            profile.getGender() != null ? profile.getGender().name() : null
        );
    }
}
