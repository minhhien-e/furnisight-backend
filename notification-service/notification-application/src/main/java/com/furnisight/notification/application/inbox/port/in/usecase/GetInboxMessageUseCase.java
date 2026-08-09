package com.furnisight.notification.application.inbox.port.in.usecase;

import com.furnisight.notification.application.common.port.in.UseCase;
import com.furnisight.notification.application.inbox.port.in.dto.response.InboxMessageResponse;
import com.furnisight.notification.application.inbox.port.in.dto.query.GetInboxMessageQuery;

import java.util.List;

public interface GetInboxMessageUseCase extends UseCase<GetInboxMessageQuery, List<InboxMessageResponse>> {
}
