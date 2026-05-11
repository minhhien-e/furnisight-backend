package com.furnisight.user.application.profile.dto;

import java.util.UUID;

public record RemoveContactCommand(
        UUID accountId,
        String type
) {}
