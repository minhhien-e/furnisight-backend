package com.furnisight.notification.application.inbox.port.in.usecase;

import com.furnisight.notification.application.common.port.in.UseCase;
import com.furnisight.notification.application.inbox.port.in.dto.command.DeleteInboxMessageCommand;

public interface DeleteInboxMessageUseCase  extends UseCase<DeleteInboxMessageCommand,Void> {
}
