package com.furnisight.promotion.domain.entities;

import com.furnisight.promotion.domain.enums.ComboDiscountType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "promotion_combos")
@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PromotionCombo {
    @Id
    private UUID id;
    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;
    private UUID imageMediaId;
    @Column(columnDefinition = "TEXT")
    private String imageUrl;
    @Enumerated(EnumType.STRING)
    private ComboDiscountType discountType;
    private double discountValue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean active;
    @Column(columnDefinition = "TEXT")
    private String placements;
    private double originalAmount;
    private double finalAmount;
    private double savedAmount;
    private long usedCount;
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
