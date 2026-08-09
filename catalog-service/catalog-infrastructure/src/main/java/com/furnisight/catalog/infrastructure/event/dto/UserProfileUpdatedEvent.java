package com.furnisight.catalog.infrastructure.event.dto;

import java.util.UUID;

public record UserProfileUpdatedEvent(
    UUID accountId,
    String firstName,
    String lastName,
    UUID avatarMediaId
) {
}
