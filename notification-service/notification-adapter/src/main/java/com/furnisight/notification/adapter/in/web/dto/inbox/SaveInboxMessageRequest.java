package com.furnisight.notification.adapter.in.web.dto.inbox;

import com.furnisight.notification.domain.model.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveInboxMessageRequest {

    @NotBlank(message = "Title is mandatory")
    private String title;

    @NotBlank(message = "Body is mandatory")
    private String body;

    private String image;
    private String actionUrl;

    @NotNull(message = "Type is mandatory")
    private NotificationType type;
}
