package com.furnisight.user.application.account.dto;

public record ResetPasswordCommand(
        String token,
        String newPassword
) {}
