package com.furnisight.notification.application.inbox.port.in.dto.query;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class GetInboxMessageQuery{
    private UUID userId;
    private int limit;
    private LocalDateTime startDate;
    private boolean isDeleted;
}
