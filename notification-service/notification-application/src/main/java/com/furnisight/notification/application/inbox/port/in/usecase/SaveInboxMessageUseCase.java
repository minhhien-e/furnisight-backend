package com.furnisight.notification.application.inbox.port.in.usecase;

import com.furnisight.notification.application.common.port.in.UseCase;
import com.furnisight.notification.application.inbox.port.in.dto.command.SaveInboxMessageCommand;
import com.furnisight.notification.application.inbox.port.in.dto.projection.InboxMessageProjection;

public interface SaveInboxMessageUseCase  extends UseCase<SaveInboxMessageCommand, InboxMessageProjection> {
}
