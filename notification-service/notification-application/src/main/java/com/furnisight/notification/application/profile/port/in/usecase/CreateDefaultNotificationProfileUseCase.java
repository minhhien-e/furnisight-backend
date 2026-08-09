package com.furnisight.notification.application.profile.port.in.usecase;

import com.furnisight.notification.application.common.port.in.UseCase;
import com.furnisight.notification.application.profile.port.in.command.CreateDefaultNotificationProfileCommand;
import com.furnisight.notification.application.profile.port.in.dto.response.NotificationProfileResponse;

public interface CreateDefaultNotificationProfileUseCase extends UseCase<CreateDefaultNotificationProfileCommand, NotificationProfileResponse> {
}
