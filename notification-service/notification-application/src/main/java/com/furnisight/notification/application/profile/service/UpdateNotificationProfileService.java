package com.furnisight.notification.application.profile.service;

import com.furnisight.notification.application.profile.port.in.command.UpdateNotificationProfileCommand;
import com.furnisight.notification.application.profile.port.in.dto.projection.NotificationProfileProjection;
import com.furnisight.notification.application.profile.port.in.usecase.UpdateNotificationProfileUseCase;
import com.furnisight.notification.application.profile.port.out.repository.NotificationProfileRepository;
import com.furnisight.notification.domain.model.entity.NotificationProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateNotificationProfileService implements UpdateNotificationProfileUseCase {

    private final NotificationProfileRepository notificationProfileRepository;

    @Override
    @Transactional
    public NotificationProfileProjection execute(UpdateNotificationProfileCommand command) {
        NotificationProfile preference = notificationProfileRepository.findByUserId(command.getUserId());

        NotificationProfile saved = notificationProfileRepository.save(preference);
        return NotificationProfileProjection.from(saved);
    }
}
