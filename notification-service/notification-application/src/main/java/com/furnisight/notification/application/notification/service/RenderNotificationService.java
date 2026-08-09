package com.furnisight.notification.application.notification.service;

import com.furnisight.notification.application.notification.port.in.dto.command.RenderNotificationCommand;
import com.furnisight.notification.application.notification.port.in.usecase.RenderNotificationUseCase;
import com.furnisight.notification.application.template.port.out.repository.NotificationTemplateRepository;
import com.furnisight.notification.domain.model.entity.RenderResult;
import com.furnisight.notification.domain.service.TemplateRender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RenderNotificationService implements RenderNotificationUseCase {
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final TemplateRender templateRender;

    @Override
    public RenderResult execute(RenderNotificationCommand command) {
        var template = notificationTemplateRepository.findByCode(command.getTemplateCode());
        return templateRender.render(template, command.getData());
    }
}
