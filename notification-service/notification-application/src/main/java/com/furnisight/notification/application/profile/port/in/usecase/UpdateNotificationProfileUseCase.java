package com.furnisight.notification.application.profile.port.in.usecase;

import com.furnisight.notification.application.common.port.in.UseCase;
import com.furnisight.notification.application.profile.port.in.command.UpdateNotificationProfileCommand;
import com.furnisight.notification.application.profile.port.in.dto.response.NotificationProfileResponse;

public interface UpdateNotificationProfileUseCase extends UseCase<UpdateNotificationProfileCommand, NotificationProfileResponse> {
}
