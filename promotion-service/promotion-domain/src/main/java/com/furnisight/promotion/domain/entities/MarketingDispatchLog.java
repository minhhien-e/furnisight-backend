package com.furnisight.promotion.domain.entities;

import com.furnisight.promotion.domain.enums.DispatchStatus;
import com.furnisight.promotion.domain.enums.MarketingChannel;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "marketing_dispatch_logs")
@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MarketingDispatchLog {
    @Id
    private UUID id;
    private String sourceType;
    private UUID sourceId;
    private UUID userId;
    @Enumerated(EnumType.STRING)
    private MarketingChannel channel;
    @Enumerated(EnumType.STRING)
    private DispatchStatus status;
    private String title;
    @Column(columnDefinition = "TEXT")
    private String message;
    @Column(columnDefinition = "TEXT")
    private String error;
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (id == null) id = UUID.randomUUID();
        createdAt = LocalDateTime.now();
    }
}
