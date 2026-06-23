package com.furnisight.admin.notification.application;

import com.furnisight.admin.notification.infrastructure.NotificationAdminClient;
import com.furnisight.admin.notification.web.dto.CreateNotificationTemplateRequest;
import com.furnisight.admin.notification.web.dto.NotificationTemplateResponse;
import com.furnisight.admin.notification.web.dto.UpdateNotificationTemplateRequest;
import com.furnisight.admin.shared.web.ActionResultResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationTemplateService {
    private final NotificationAdminClient notificationAdminClient;

    public List<NotificationTemplateResponse> getTemplates(String name, String channel, String type) {
        return notificationAdminClient.getTemplates(name, channel, type);
    }

    public NotificationTemplateResponse getTemplateByCode(String code) {
        return notificationAdminClient.getTemplateByCode(code);
    }

    public NotificationTemplateResponse createTemplate(CreateNotificationTemplateRequest request) {
        return notificationAdminClient.createTemplate(request);
    }

    public NotificationTemplateResponse updateTemplate(UUID templateId, UpdateNotificationTemplateRequest request) {
        return notificationAdminClient.updateTemplate(templateId, request);
    }

    public ActionResultResponse deleteTemplate(UUID templateId) {
        return notificationAdminClient.deleteTemplate(templateId);
    }
}
