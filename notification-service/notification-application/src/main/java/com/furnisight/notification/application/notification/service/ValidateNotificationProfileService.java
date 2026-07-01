package com.furnisight.notification.application.notification.service;

import com.furnisight.notification.application.notification.port.in.dto.command.ValidateNotificationProfileCommand;
import com.furnisight.notification.application.notification.port.in.usecase.ValidateNotificationProfileUseCase;
import com.furnisight.notification.application.profile.port.out.repository.NotificationProfileRepository;
import com.furnisight.notification.domain.exceptions.NotFoundException;
import com.furnisight.notification.domain.exceptions.ErrorCode;
import com.furnisight.notification.domain.model.entity.NotificationProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ValidateNotificationProfileService implements ValidateNotificationProfileUseCase {

    private final NotificationProfileRepository notificationProfileRepository;

    @Override
    @Transactional
    public boolean execute(ValidateNotificationProfileCommand command) {
        var preference = getOrCreateDefaultProfile(command);
        return preference.canReceiveNotificationType(command.getNotificationType());
    }

    private NotificationProfile getOrCreateDefaultProfile(ValidateNotificationProfileCommand command) {
        try {
            return notificationProfileRepository.findByUserId(command.getUserId());
        } catch (NotFoundException ignored) {
            return notificationProfileRepository.save(NotificationProfile.builder()
                    .id(command.getUserId())
                    .userId(command.getUserId())
                    .orderUpdatesEnabled(true)
                    .promotionsEnabled(true)
                    .walletUpdatesEnabled(true)
                    .socialUpdatesEnabled(true)
                    .build());
        }
    }
}
