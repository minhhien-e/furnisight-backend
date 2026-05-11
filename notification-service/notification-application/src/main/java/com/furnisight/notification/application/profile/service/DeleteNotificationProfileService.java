package com.furnisight.notification.application.profile.service;

import com.furnisight.notification.application.profile.port.in.command.DeleteNotificationProfileCommand;
import com.furnisight.notification.application.profile.port.in.usecase.DeleteNotificationProfileUseCase;
import com.furnisight.notification.application.profile.port.out.repository.NotificationProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteNotificationProfileService implements DeleteNotificationProfileUseCase {

    private final NotificationProfileRepository notificationProfileRepository;

    @Override
    @Transactional
    public Void execute(DeleteNotificationProfileCommand command) {
        notificationProfileRepository.deleteByUserId(command.getUserId());
        return null;
    }
}
