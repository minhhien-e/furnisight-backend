package com.furnisight.notification.application.template.service;

import com.furnisight.notification.application.template.port.in.usecase.FindNotificationTemplateByCodeUseCase;
import com.furnisight.notification.application.template.port.in.dto.query.FindNotificationTemplateByCodeQuery;
import com.furnisight.notification.application.template.port.in.dto.projection.NotificationTemplateProjection;
import com.furnisight.notification.application.template.port.out.repository.NotificationTemplateRepository;
import com.furnisight.notification.domain.model.entity.NotificationTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindNotificationTemplateByCodeService implements FindNotificationTemplateByCodeUseCase {

    private final NotificationTemplateRepository notificationTemplateRepository;

    @Override
    @Transactional(readOnly = true)
    public NotificationTemplateProjection execute(FindNotificationTemplateByCodeQuery query) {
        return NotificationTemplateProjection.from(notificationTemplateRepository.findByCode(query.getCode()));
    }
}
