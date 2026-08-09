package com.furnisight.notification.application.template.service;

import com.furnisight.notification.application.template.port.in.dto.command.DeleteNotificationTemplateCommand;
import com.furnisight.notification.application.template.port.in.usecase.DeleteNotificationTemplateUseCase;
import com.furnisight.notification.application.template.port.out.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteNotificationTemplateService implements DeleteNotificationTemplateUseCase {

    private final NotificationTemplateRepository notificationTemplateRepository;

    @Override
    @Transactional
    public Void execute(DeleteNotificationTemplateCommand command) {
        notificationTemplateRepository.deleteById(command.getTemplateId());
        return null;
    }
}
