package com.furnisight.user.application.account.dto;

import java.util.UUID;

public record LogoutAllAccountCommand(
        UUID accountId
) {}
