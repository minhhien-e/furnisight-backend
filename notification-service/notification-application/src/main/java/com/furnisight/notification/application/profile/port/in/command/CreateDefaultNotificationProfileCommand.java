package com.furnisight.notification.application.profile.port.in.command;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class CreateDefaultNotificationProfileCommand {
    private UUID userId;
}
