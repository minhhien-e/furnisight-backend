package com.furnisight.notification.application.notification.port.in.dto.command;
import com.furnisight.notification.domain.service.TemplateData;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RenderNotificationCommand {
    private String templateCode;
    private TemplateData data;
}
