package com.furnisight.user.application.account.dto;

import java.util.UUID;

public record RevokeRoleCommand(
        UUID adminId,
        UUID targetAccountId,
        UUID roleId
) {}
