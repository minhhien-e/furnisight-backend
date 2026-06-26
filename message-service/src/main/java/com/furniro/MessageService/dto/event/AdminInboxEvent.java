package com.furniro.MessageService.dto.event;

import java.time.LocalDateTime;

import com.furniro.MessageService.database.entity.Conversation;
import com.furniro.MessageService.database.entity.Message;
import com.furniro.MessageService.util.enums.ConversationChannel;
import com.furniro.MessageService.util.enums.ConversationPriority;
import com.furniro.MessageService.util.enums.ConversationStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminInboxEvent {
    private String eventType;
    private Integer id;
    private Integer buyerId;
    private Integer staffId;
    private Integer assignedAdminId;
    private ConversationChannel channel;
    private ConversationStatus status;
    private ConversationPriority priority;
    private String lastMessageContent;
    private LocalDateTime lastMessageAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer messageId;
    private Integer senderId;
    private Boolean internal;

    public static AdminInboxEvent fromConversation(String eventType, Conversation conversation, Message message) {
        Integer senderId = message != null ? message.getSenderId() : conversation.getBuyerId();
        return AdminInboxEvent.builder()
                .eventType(eventType)
                .id(conversation.getId())
                .buyerId(conversation.getBuyerId())
                .staffId(conversation.getStaffId())
                .assignedAdminId(conversation.getAssignedAdminId())
                .channel(conversation.getChannel())
                .status(conversation.getStatus())
                .priority(conversation.getPriority())
                .lastMessageContent(conversation.getLastMessageContent())
                .lastMessageAt(conversation.getLastMessageAt())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .messageId(message != null ? message.getId() : null)
                .senderId(senderId)
                .internal(message != null ? message.getIsInternal() : false)
                .build();
    }
}
