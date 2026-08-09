package com.furnisight.review.adapter.in.messaging;

import java.util.UUID;

public record UserProfileUpdatedEvent(
    UUID accountId,
    String firstName,
    String lastName,
    UUID avatarMediaId
) {
}

