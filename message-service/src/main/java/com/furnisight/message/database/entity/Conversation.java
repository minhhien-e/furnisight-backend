package com.furnisight.message.database.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.ZoneId;

import com.furnisight.message.util.enums.ConversationChannel;
import com.furnisight.message.util.enums.ConversationPriority;
import com.furnisight.message.util.enums.ConversationStatus;

@Entity
@Table(name = "conversations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    private static final ZoneId HO_CHI_MINH_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer buyerId;

    private Integer staffId;

    @Column(length = 32)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ConversationChannel channel = ConversationChannel.SUPPORT;

    @Column(length = 32)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ConversationStatus status = ConversationStatus.OPEN;

    @Column(length = 32)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ConversationPriority priority = ConversationPriority.MEDIUM;

    private Integer assignedAdminId;

    @Builder.Default
    private LocalDateTime lastMessageAt = LocalDateTime.now(HO_CHI_MINH_ZONE);

    
    @Builder.Default
    private String lastMessageContent = "";

    private LocalDateTime closedAt;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now(HO_CHI_MINH_ZONE);

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now(HO_CHI_MINH_ZONE);

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now(HO_CHI_MINH_ZONE);
        this.updatedAt = LocalDateTime.now(HO_CHI_MINH_ZONE);
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now(HO_CHI_MINH_ZONE);
    }
}
