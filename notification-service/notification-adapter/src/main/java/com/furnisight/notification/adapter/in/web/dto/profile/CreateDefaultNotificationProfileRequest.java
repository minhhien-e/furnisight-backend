package com.furnisight.notification.adapter.in.web.dto.preference;

import jakarta.validation.constraints.Email;
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
public class CreateDefaultNotificationProfileRequest {
    @Email(message = "email is invalid")
    private String userEmail;
}
