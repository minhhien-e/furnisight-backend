package com.furnisight.media.core.model.event;

import com.furnisight.media.core.model.enums.ProcessingJobType;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record MediaProcessingCompletedEvent(
    UUID mediaId,
    ProcessingJobType jobType,
    Map<String, Object> outputs, // resize paths, thumbnails, etc.
    Instant occurredAt
) implements DomainEvent, InternalEvent {}
