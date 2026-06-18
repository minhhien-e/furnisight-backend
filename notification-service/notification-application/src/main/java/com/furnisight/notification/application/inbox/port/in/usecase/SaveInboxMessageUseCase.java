package com.furnisight.notification.application.inbox.port.in.usecase;

import com.furnisight.notification.application.common.port.in.UseCase;
import com.furnisight.notification.application.inbox.port.in.dto.command.SaveInboxMessageCommand;
import com.furnisight.notification.application.inbox.port.in.dto.response.InboxMessageResponse;

public interface SaveInboxMessageUseCase  extends UseCase<SaveInboxMessageCommand, InboxMessageResponse> {
}
