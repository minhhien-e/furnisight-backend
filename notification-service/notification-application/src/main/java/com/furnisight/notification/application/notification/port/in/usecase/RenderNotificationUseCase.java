package com.furnisight.notification.application.notification.port.in.usecase;

import com.furnisight.notification.application.common.port.in.UseCase;
import com.furnisight.notification.application.notification.port.in.dto.command.RenderNotificationCommand;
import com.furnisight.notification.domain.model.entity.RenderResult;

public interface RenderNotificationUseCase extends UseCase<RenderNotificationCommand, RenderResult> {
    RenderResult execute(RenderNotificationCommand command);
}
