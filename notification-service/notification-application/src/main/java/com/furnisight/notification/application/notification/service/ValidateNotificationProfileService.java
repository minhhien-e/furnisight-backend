package com.furnisight.notification.application.notification.service;

import com.furnisight.notification.application.notification.port.in.dto.command.ValidateNotificationProfileCommand;
import com.furnisight.notification.application.notification.port.in.usecase.ValidateNotificationProfileUseCase;
import com.furnisight.notification.application.profile.port.out.repository.NotificationProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ValidateNotificationProfileService implements ValidateNotificationProfileUseCase {

    private final NotificationProfileRepository notificationProfileRepository;

    @Override
    public boolean execute(ValidateNotificationProfileCommand command) {
        var preference = notificationProfileRepository.findByUserId(command.getUserId());
        return preference.canReceiveNotificationType(command.getNotificationType());
    }
}
