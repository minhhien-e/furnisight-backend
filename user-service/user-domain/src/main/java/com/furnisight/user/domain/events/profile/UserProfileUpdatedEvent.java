package com.furnisight.user.domain.events.profile;

import com.furnisight.user.domain.seedwork.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class UserProfileUpdatedEvent implements DomainEvent {

    private final UUID accountId;
    private final String firstName;
    private final String lastName;
    private final UUID avatarMediaId;
    private final LocalDateTime occurredOn;

    public UserProfileUpdatedEvent(UUID accountId, String firstName, String lastName, UUID avatarMediaId) {
        this.accountId = accountId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.avatarMediaId = avatarMediaId;
        this.occurredOn = LocalDateTime.now();
    }
}
