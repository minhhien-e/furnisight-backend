package com.furnisight.order.domain.entities.promotion;

import com.furnisight.order.domain.enums.DiscountType;
import com.furnisight.order.domain.seedwork.DomainEntity;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.*;

@Entity
@Table(name = "promotions")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class Promotion extends DomainEntity {
    @Id
    private UUID id;
    
    @Column(unique = true)
    private String code;
    
    private String name;
    private String description;
    private String icon;
    
    @Enumerated(EnumType.STRING)
    private DiscountType discountType;
    private Double discountValue;
    private Double maxDiscount;
    private Double minOrder;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean active;
}
