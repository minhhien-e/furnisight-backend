package com.furnisight.catalog.domain.events.product;

import com.furnisight.catalog.domain.seedwork.DomainEvent;
import com.furnisight.catalog.domain.enums.product.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductStatusUpdatedEvent implements DomainEvent {
    @Builder.Default
    private UUID eventId = UUID.randomUUID();
    private UUID shopId;
    private UUID productId;
    private ProductStatus newStatus;
    private LocalDateTime occurredAt;

    @Override
    public String getType() {
        return "PRODUCT_STATUS_UPDATED";
    }
}

