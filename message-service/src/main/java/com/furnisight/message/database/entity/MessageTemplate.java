package com.furnisight.message.database.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "message_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageTemplate {

    private static final ZoneId HO_CHI_MINH_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(length = 50)
    private String category;

    @Builder.Default
    private Boolean active = true;

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
