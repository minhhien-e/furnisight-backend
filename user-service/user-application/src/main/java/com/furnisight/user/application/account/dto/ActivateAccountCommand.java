package com.furnisight.user.application.account.dto;

import java.util.UUID;

public record ActivateAccountCommand(
        UUID adminId,
        UUID targetAccountId
) {}
