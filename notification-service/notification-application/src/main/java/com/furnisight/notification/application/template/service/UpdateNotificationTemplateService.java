package com.furnisight.notification.application.template.service;

import com.furnisight.notification.application.template.port.in.usecase.UpdateNotificationTemplateUseCase;
import com.furnisight.notification.application.template.port.in.dto.command.UpdateNotificationTemplateCommand;
import com.furnisight.notification.application.template.port.in.dto.projection.NotificationTemplateProjection;
import com.furnisight.notification.application.template.port.out.repository.NotificationTemplateRepository;
import com.furnisight.notification.domain.model.entity.NotificationTemplate;
import com.furnisight.notification.domain.exception.NotificationTemplateNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UpdateNotificationTemplateService implements UpdateNotificationTemplateUseCase {

    private final NotificationTemplateRepository notificationTemplateRepository;

    @Override
    @Transactional
    public NotificationTemplateProjection execute(UpdateNotificationTemplateCommand command) {
        NotificationTemplate template = notificationTemplateRepository.findById(command.getTemplateId())
                .orElseThrow(() -> new NotificationTemplateNotFoundException(command.getTemplateId()));

        if (StringUtils.hasText(command.getName())) {
            template.setName(command.getName());
        }
        if (StringUtils.hasText(command.getTitleTemplate())) {
            template.setTitleTemplate(command.getTitleTemplate());
        }
        if (StringUtils.hasText(command.getBodyTemplate())) {
            template.setBodyTemplate(command.getBodyTemplate());
        }

        NotificationTemplate saved = notificationTemplateRepository.save(template);
        return NotificationTemplateProjection.from(saved);
    }
}
