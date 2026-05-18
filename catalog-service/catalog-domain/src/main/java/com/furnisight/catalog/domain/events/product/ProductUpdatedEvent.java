package com.furnisight.catalog.domain.events.product;

import com.furnisight.catalog.domain.seedwork.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductUpdatedEvent implements DomainEvent {
    @Builder.Default
    private UUID eventId = UUID.randomUUID();
    private UUID shopId;
    private UUID productId;
    private String productName;
    private String description;
    private Double weight;
    private Double length;
    private Double height;
    private Double width;
    private Map<String, Object> attributes;
    private List<ProductVariantDto> variants;
    private LocalDateTime occurredAt;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductVariantDto {
        private String sku;
        private Double price;
        private Integer stockQuantity;
    }
}
