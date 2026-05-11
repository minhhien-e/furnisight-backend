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
    String phone,
    LocalDate dateOfBirth,
    String gender
) {
    public static ProfileResponse from(UserProfile profile) {
        return new ProfileResponse(
            profile.getId(),
            profile.getAccountId(),
            profile.getDisplayName(),
            profile.getFirstName(),
            profile.getLastName(),
            profile.getAvatarUrl(),
            profile.getEmail() != null ? profile.getEmail().getValue() : null,
            profile.getPhoneNumber() != null ? profile.getPhoneNumber().getValue() : null,
            profile.getDateOfBirth(),
            profile.getGender() != null ? profile.getGender().name() : null
        );
    }
}
