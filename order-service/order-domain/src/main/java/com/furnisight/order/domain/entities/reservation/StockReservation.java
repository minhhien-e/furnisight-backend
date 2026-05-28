package com.furnisight.order.domain.entities.reservation;

import com.furnisight.order.domain.seedwork.DomainEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stock_reservations")
@Data
@lombok.EqualsAndHashCode(callSuper=false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockReservation extends DomainEntity {

    @Id
    private UUID id;
    
    private String orderCode;
    
    private UUID productId;
    
    private UUID productVariantId;
    
    private int quantity;
    
    private LocalDateTime createdAt;

    @Builder
    public StockReservation(UUID id, String orderCode, UUID productId, UUID productVariantId, int quantity) {
        this.id = id != null ? id : UUID.randomUUID();
        this.orderCode = orderCode;
        this.productId = productId;
        this.productVariantId = productVariantId;
        this.quantity = quantity;
        this.createdAt = LocalDateTime.now();
    }
}
