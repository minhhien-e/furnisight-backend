package com.furnisight.catalog.domain.entities;

import com.furnisight.catalog.domain.seedwork.AggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "product_favorite_logs")
public class ProductFavoriteLog extends AggregateRoot {
    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public ProductFavoriteLog(UUID userId, UUID productId) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.productId = productId;
        this.createdAt = LocalDateTime.now();
    }
}
