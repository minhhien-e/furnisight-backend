package com.furnisight.notification.application.template.port.in.usecase;

import com.furnisight.notification.application.common.port.in.UseCase;
import com.furnisight.notification.application.template.port.in.dto.query.FilterNotificationTemplateQuery;
import com.furnisight.notification.application.template.port.in.dto.response.NotificationTemplateResponse;

import java.util.List;

public interface FilterNotificationTemplateUseCase extends UseCase<FilterNotificationTemplateQuery, List<NotificationTemplateResponse>> {
}
