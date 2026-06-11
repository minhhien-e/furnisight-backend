package com.furnisight.promotion.domain.entities;

import com.furnisight.promotion.domain.enums.CampaignStatus;
import com.furnisight.promotion.domain.enums.MarketingSendType;
import com.furnisight.promotion.domain.enums.MarketingTargetType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "marketing_campaigns")
@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MarketingCampaign {
    @Id
    private UUID id;
    private String name;
    private UUID voucherId;
    @Enumerated(EnumType.STRING)
    private MarketingTargetType targetType;
    @Column(columnDefinition = "TEXT")
    private String targetUserIds;
    private String segmentKey;
    @Column(columnDefinition = "TEXT")
    private String channels;
    @Enumerated(EnumType.STRING)
    private MarketingSendType scheduleType;
    private LocalDateTime scheduledAt;
    private String notificationTitle;
    @Column(columnDefinition = "TEXT")
    private String notificationBody;
    @Enumerated(EnumType.STRING)
    private CampaignStatus status;
    private long sentCount;
    private boolean active;
    private LocalDateTime dispatchedAt;
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
