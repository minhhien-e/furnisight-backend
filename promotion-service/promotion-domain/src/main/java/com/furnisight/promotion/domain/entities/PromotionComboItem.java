package com.furnisight.promotion.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "promotion_combo_items")
@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PromotionComboItem {
    @Id
    private UUID id;
    private UUID comboId;
    private String productId;
    private String variantId;
    private String productSlug;
    private String productName;
    private String sku;
    private String categoryName;
    private String image;
    private double price;
    private int quantity;
    private boolean snapshotMissing;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (id == null) id = UUID.randomUUID();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
