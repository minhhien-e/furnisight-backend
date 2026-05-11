package com.furnisight.notification.application.notification.port.in.dto.command;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class RenderNotificationCommand {
    private String templateCode;
    private Map<String, Object> data;
}
