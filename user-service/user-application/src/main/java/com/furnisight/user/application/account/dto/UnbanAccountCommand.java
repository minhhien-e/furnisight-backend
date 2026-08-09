package com.furnisight.user.application.account.dto;

import java.util.UUID;

public record UnbanAccountCommand(
        UUID adminId,
        UUID targetAccountId
) {}
