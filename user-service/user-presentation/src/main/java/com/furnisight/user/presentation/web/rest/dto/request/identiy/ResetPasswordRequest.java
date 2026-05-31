package com.furnisight.user.presentation.web.rest.dto.request.identiy;

public record ResetPasswordRequest(
        String email,
        String token,
        String newPassword
) {}
