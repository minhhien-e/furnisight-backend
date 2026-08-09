package com.furnisight.notification.application.inbox.port.in.usecase;

import com.furnisight.notification.application.common.port.in.UseCase;
import com.furnisight.notification.application.inbox.port.in.dto.command.MarkAllInboxMessagesAsReadCommand;

public interface MarkAllInboxMessagesAsReadUseCase  extends UseCase<MarkAllInboxMessagesAsReadCommand, Void> {
}
