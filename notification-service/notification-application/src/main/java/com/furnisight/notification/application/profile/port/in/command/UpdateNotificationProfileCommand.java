package com.furnisight.notification.application.profile.port.in.command;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class UpdateNotificationProfileCommand {
    private final UUID userId;
}
