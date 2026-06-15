package com.furnisight.user.application.account.dto;

import java.util.UUID;

public record ChangePasswordCommand(
        UUID accountId,
        String currentPassword,
        String newPassword
) {}
