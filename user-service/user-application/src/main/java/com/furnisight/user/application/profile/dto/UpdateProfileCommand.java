package com.furnisight.user.application.profile.dto;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateProfileCommand(
        UUID accountId,
        String displayName,
        String fullName,
        UUID avatarMediaId,
        String bio,
        LocalDate dateOfBirth,
        String gender) {
}
