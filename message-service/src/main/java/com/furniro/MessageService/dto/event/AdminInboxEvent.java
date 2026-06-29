package com.furniro.MessageService.dto.event;

import java.time.LocalDateTime;

import com.furniro.MessageService.database.entity.Conversation;
import com.furniro.MessageService.database.entity.Message;
import com.furniro.MessageService.dto.res.CustomerProfile;
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
    private String buyerName;
    private String buyerEmail;
    private String buyerAvatarUrl;
    private Integer staffId;
    private Integer assignedAdminId;
    private ConversationChannel channel;
    private ConversationStatus status;
    private ConversationPriority priority;
    private String lastMessageContent;
    private LocalDateTime lastMessageAt;
    private LocalDateTime closedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer messageId;
    private Integer senderId;
    private Boolean internal;

    public static AdminInboxEvent fromConversation(String eventType, Conversation conversation, Message message) {
        return fromConversation(eventType, conversation, message, null);
    }

    public static AdminInboxEvent fromConversation(
            String eventType,
            Conversation conversation,
            Message message,
            CustomerProfile profile) {
        Integer senderId = message != null ? message.getSenderId() : conversation.getBuyerId();
        return AdminInboxEvent.builder()
                .eventType(eventType)
                .id(conversation.getId())
                .buyerId(conversation.getBuyerId())
                .buyerName(profile != null ? profile.getBuyerName() : null)
                .buyerEmail(profile != null ? profile.getBuyerEmail() : null)
                .buyerAvatarUrl(profile != null ? profile.getBuyerAvatarUrl() : null)
                .staffId(conversation.getStaffId())
                .assignedAdminId(conversation.getAssignedAdminId())
                .channel(conversation.getChannel())
                .status(conversation.getStatus())
                .priority(conversation.getPriority())
                .lastMessageContent(conversation.getLastMessageContent())
                .lastMessageAt(conversation.getLastMessageAt())
                .closedAt(resolveClosedAt(conversation))
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .messageId(message != null ? message.getId() : null)
                .senderId(senderId)
                .internal(message != null ? message.getIsInternal() : false)
                .build();
    }

    private static LocalDateTime resolveClosedAt(Conversation conversation) {
        if (conversation.getClosedAt() != null) {
            return conversation.getClosedAt();
        }
        if (ConversationStatus.CLOSED.equals(conversation.getStatus())) {
            return conversation.getUpdatedAt();
        }
        return null;
    }
}
