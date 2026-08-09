package com.furnisight.notification.application.template.service;

import com.furnisight.notification.application.template.port.in.dto.command.CreateNotificationTemplateCommand;
import com.furnisight.notification.application.template.port.in.usecase.CreateNotificationTemplateUseCase;
import com.furnisight.notification.application.template.port.in.dto.response.NotificationTemplateResponse;
import com.furnisight.notification.application.template.port.out.repository.NotificationTemplateRepository;
import com.furnisight.notification.domain.model.entity.NotificationTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateNotificationTemplateService implements CreateNotificationTemplateUseCase {

    private final NotificationTemplateRepository notificationTemplateRepository;

    @Override
    @Transactional
    public NotificationTemplateResponse execute(CreateNotificationTemplateCommand command) {
        NotificationTemplate template = NotificationTemplate.builder()
                .code(command.getCode())
                .name(command.getName())
                .titleTemplate(command.getTitleTemplate())
                .bodyTemplate(command.getBodyTemplate())
                .type(command.getType())
                .channel(command.getChannel())
                .defaultImage(command.getDefaultImage())
                .defaultActionUrl(command.getDefaultActionUrl())
                .build();

        NotificationTemplate saved = notificationTemplateRepository.save(template);
        return NotificationTemplateResponse.from(saved);
    }
}
