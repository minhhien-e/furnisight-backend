package com.furnisight.notification.domain.service;

import com.furnisight.notification.domain.model.entity.NotificationTemplate;
import com.furnisight.notification.domain.model.entity.RenderResult;
import org.springframework.stereotype.Service;

@Service
public class TemplateRender {
    public RenderResult render(NotificationTemplate template, TemplateData data) {
        String body = template.getBodyTemplate();
        String subject = template.getTitleTemplate();

        for (String key : template.getVariables()) {
            String value = data.resolve(key);
            body = body.replace("{{" + key + "}}", value.toString());
            subject = subject.replace("{{" + key + "}}", value.toString());
        }

        return RenderResult.builder().title(subject).body(body).build();
    }

}
