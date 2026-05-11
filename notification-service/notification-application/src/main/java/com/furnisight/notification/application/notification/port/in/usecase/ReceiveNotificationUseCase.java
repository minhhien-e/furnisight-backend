package com.furnisight.notification.application.notification.port.in.usecase;

import com.furnisight.notification.application.common.port.in.UseCase;
import com.furnisight.notification.application.notification.port.in.dto.command.ReceiveNotificationCommand;

public interface ReceiveNotificationUseCase extends UseCase<ReceiveNotificationCommand,Void> {
    Void execute(ReceiveNotificationCommand command);
}
