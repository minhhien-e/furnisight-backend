package com.furnisight.catalog.domain.events.category;

import com.furnisight.catalog.domain.seedwork.DomainEvent;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class CategoryCreateEvent implements DomainEvent {
    @Builder.Default
    private UUID eventId = UUID.randomUUID();
    private UUID categoryId;
    private String name;
    private String slug;
    private UUID parentId;
    private LocalDateTime occurredAt;
}
