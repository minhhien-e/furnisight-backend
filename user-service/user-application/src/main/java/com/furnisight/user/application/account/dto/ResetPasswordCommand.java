package com.furnisight.user.application.account.dto;

public record ResetPasswordCommand(
        String email,
        String token,
        String newPassword
) {}
