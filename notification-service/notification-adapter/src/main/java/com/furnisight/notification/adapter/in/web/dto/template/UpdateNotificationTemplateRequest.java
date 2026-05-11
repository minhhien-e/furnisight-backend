package com.furnisight.notification.adapter.in.web.dto.template;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateNotificationTemplateRequest {
    @NotBlank(message = "Name is mandatory")
    private String name;

    @NotBlank(message = "Title template is mandatory")
    private String titleTemplate;

    @NotBlank(message = "Body template is mandatory")
    private String bodyTemplate;
}
