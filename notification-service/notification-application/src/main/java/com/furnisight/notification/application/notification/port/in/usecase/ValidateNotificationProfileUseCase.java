package com.furnisight.notification.application.notification.port.in.usecase;

import com.furnisight.notification.application.notification.port.in.dto.command.ValidateNotificationProfileCommand;

public interface ValidateNotificationProfileUseCase {
    boolean execute(ValidateNotificationProfileCommand command);
}
