package com.furniro.MessageService.database.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

import com.furniro.MessageService.util.enums.MessageType;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    @JsonIgnore
    private Conversation conversation;

    @NotNull
    private Integer senderId;

    @NotNull
    private Integer receiverId;

    @NotBlank
    @Size(max = 2000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MessageType type = MessageType.TEXT;

    private Integer fileId;

    private String mediaId;

    @Size(max = 2000)
    private String attachmentUrl;

    @Size(max = 255)
    private String attachmentName;

    @Size(max = 120)
    private String attachmentType;

    private Long attachmentSize;

    @Builder.Default
    private Boolean isInternal = false;

    @Builder.Default
    private Boolean isRead = false;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
