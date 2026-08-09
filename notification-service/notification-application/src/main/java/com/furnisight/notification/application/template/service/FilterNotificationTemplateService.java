package com.furnisight.notification.application.template.service;

import com.furnisight.notification.application.template.port.in.usecase.FilterNotificationTemplateUseCase;
import com.furnisight.notification.application.template.port.in.dto.query.FilterNotificationTemplateQuery;
import com.furnisight.notification.application.template.port.in.dto.response.NotificationTemplateResponse;
import com.furnisight.notification.application.template.port.out.repository.NotificationTemplateRepository;
import com.furnisight.notification.domain.model.entity.NotificationTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilterNotificationTemplateService implements FilterNotificationTemplateUseCase {

    private final NotificationTemplateRepository notificationTemplateRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationTemplateResponse> execute(FilterNotificationTemplateQuery query) {
        return notificationTemplateRepository.filter(query.getName(), query.getChannel(), query.getType())
            .stream()
            .map(NotificationTemplateResponse::from)
            .collect(Collectors.toList());
    }
}
