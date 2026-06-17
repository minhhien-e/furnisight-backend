package com.furnisight.notification.application.profile.service;

import com.furnisight.notification.application.profile.port.in.command.CreateDefaultNotificationProfileCommand;
import com.furnisight.notification.application.profile.port.in.dto.projection.NotificationProfileProjection;
import com.furnisight.notification.application.profile.port.in.usecase.CreateDefaultNotificationProfileUseCase;
import com.furnisight.notification.application.profile.port.out.repository.NotificationProfileRepository;
import com.furnisight.notification.domain.exception.NotificationProfileNotFoundException;
import com.furnisight.notification.domain.model.entity.NotificationProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateDefaultNotificationProfileService implements CreateDefaultNotificationProfileUseCase {

    private final NotificationProfileRepository notificationProfileRepository;

    @Override
    @Transactional
    public NotificationProfileProjection execute(CreateDefaultNotificationProfileCommand command) {
        try {
            return NotificationProfileProjection.from(notificationProfileRepository.findByUserId(command.getUserId()));
        } catch (NotificationProfileNotFoundException ignored) {
            // Create the default profile below.
        }

        NotificationProfile preference = NotificationProfile.builder()
                .id(command.getUserId())
                .userId(command.getUserId())
                .orderUpdatesEnabled(true)
                .promotionsEnabled(true)
                .walletUpdatesEnabled(true)
                .socialUpdatesEnabled(true)
                .build();

        NotificationProfile saved = notificationProfileRepository.save(preference);
        return NotificationProfileProjection.from(saved);
    }
}
