package com.furnisight.user.application.account.dto;

import java.util.UUID;

public record DeleteAccountCommand(
        UUID adminId,
        UUID targetAccountId
) {}
