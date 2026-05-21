package com.furnisight.media.core.model.event;

import com.furnisight.media.core.model.enums.ProcessingJobType;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;
@Builder
public record MediaProcessingFailedEvent(
    UUID mediaId,
    ProcessingJobType jobType,
    String errorMessage,
    Instant occurredAt
) implements DomainEvent, InternalEvent {}
