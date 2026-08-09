package com.furnisight.notification.application.inbox.port.in.usecase;

import com.furnisight.notification.application.common.port.in.UseCase;
import com.furnisight.notification.application.inbox.port.in.dto.command.MarkInboxMessageAsReadCommand;

public interface MarkInboxMessageAsReadUseCase extends UseCase<MarkInboxMessageAsReadCommand, Void> {
}
