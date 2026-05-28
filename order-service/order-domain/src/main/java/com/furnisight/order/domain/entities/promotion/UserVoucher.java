package com.furnisight.order.domain.entities.promotion;

import com.furnisight.order.domain.seedwork.DomainEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.*;

@Entity
@Table(name = "user_vouchers")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserVoucher extends DomainEntity {
    @Id
    private UUID id;
    
    @Column(name = "user_id")
    private UUID userId;
    
    @Column(name = "promotion_id")
    private UUID promotionId;
    
    @Column(name = "is_used")
    private boolean isUsed;
    
    private LocalDateTime usedAt;
    private LocalDateTime savedAt;
    
    // Derived property for convenience when resolving queries
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", insertable = false, updatable = false)
    private Promotion promotion;
    
    public void markAsUsed(LocalDateTime time) {
        this.isUsed = true;
        this.usedAt = time;
    }
}
