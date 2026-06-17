package com.furnisight.user.application.account.dto;

import java.util.UUID;

public record CreateAccountCommand(
        UUID adminId,
        String email,
        String password,
        String fullName,
        UUID roleId
) {}
