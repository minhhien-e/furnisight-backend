package com.furnisight.notification.domain.service;

import com.furnisight.notification.domain.model.entity.NotificationTemplate;
import com.furnisight.notification.domain.model.entity.RenderResult;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TemplateRender {
    public RenderResult render(NotificationTemplate template, Map<String, Object> data) {
        String body = template.getBodyTemplate();
        String subject = template.getTitleTemplate();

        for (String key : template.getVariables()) {
            Object value = data.getOrDefault(key, "");
            body = body.replace("{{" + key + "}}", value.toString());
            subject = subject.replace("{{" + key + "}}", value.toString());
        }

        return RenderResult.builder().title(subject).body(body).build();
    }

}
