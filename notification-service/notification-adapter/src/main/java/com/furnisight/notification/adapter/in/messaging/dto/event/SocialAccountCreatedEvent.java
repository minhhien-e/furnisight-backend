package com.furnisight.notification.adapter.in.messaging.dto.event;

import java.util.UUID;

public record SocialAccountCreatedEvent(
    UUID accountId,
    String email,
    String password
) {
}
