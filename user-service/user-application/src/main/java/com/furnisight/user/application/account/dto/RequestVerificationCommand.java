package com.furnisight.user.application.account.dto;

import java.util.UUID;

public record RequestVerificationCommand(
        UUID accountId,
        String channel,
        String destination
) {
}
