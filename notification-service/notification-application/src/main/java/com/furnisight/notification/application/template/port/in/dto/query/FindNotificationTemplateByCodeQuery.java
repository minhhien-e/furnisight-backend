package com.furnisight.notification.application.template.port.in.dto.query;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FindNotificationTemplateByCodeQuery {
    private final String code;
}
