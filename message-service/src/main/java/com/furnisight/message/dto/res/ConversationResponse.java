package com.furnisight.message.dto.res;

import java.time.LocalDateTime;

import com.furnisight.message.database.entity.Conversation;
import com.furnisight.message.util.enums.ConversationChannel;
import com.furnisight.message.util.enums.ConversationPriority;
import com.furnisight.message.util.enums.ConversationStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConversationResponse {
    private Integer id;
    private Integer buyerId;
    private Integer staffId;
    private Integer assignedAdminId;
    private ConversationChannel channel;
    private ConversationStatus status;
    private ConversationPriority priority;
    private LocalDateTime lastMessageAt;
    private String lastMessageContent;
    private LocalDateTime closedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String buyerName;
    private String buyerEmail;
    private String buyerAvatarUrl;

    public static ConversationResponse from(Conversation conversation, CustomerProfile profile) {
        return ConversationResponse.builder()
                .id(conversation.getId())
                .buyerId(conversation.getBuyerId())
                .staffId(conversation.getStaffId())
                .assignedAdminId(conversation.getAssignedAdminId())
                .channel(conversation.getChannel())
                .status(conversation.getStatus())
                .priority(conversation.getPriority())
                .lastMessageAt(conversation.getLastMessageAt())
                .lastMessageContent(conversation.getLastMessageContent())
                .closedAt(resolveClosedAt(conversation))
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .buyerName(profile != null ? profile.getBuyerName() : null)
                .buyerEmail(profile != null ? profile.getBuyerEmail() : null)
                .buyerAvatarUrl(profile != null ? profile.getBuyerAvatarUrl() : null)
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
