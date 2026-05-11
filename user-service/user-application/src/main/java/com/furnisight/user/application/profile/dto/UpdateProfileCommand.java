package com.furnisight.user.application.profile.dto;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateProfileCommand(
        UUID accountId,
        String displayName,
        String firstName,
        String lastName,
        String avatarUrl,
        LocalDate dateOfBirth,
        String gender) {
}
