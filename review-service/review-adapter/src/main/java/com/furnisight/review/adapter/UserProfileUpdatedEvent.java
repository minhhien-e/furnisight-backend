package com.furnisight.review.adapter.event.dto;

import java.util.UUID;

public record UserProfileUpdatedEvent(
    UUID accountId,
    String firstName,
    String lastName,
    UUID avatarMediaId
) {
}

