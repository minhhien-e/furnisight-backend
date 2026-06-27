package com.furniro.MessageService.service.Conversation;

import org.springframework.stereotype.Service;

import com.furniro.MessageService.database.entity.Conversation;
import com.furniro.MessageService.database.entity.Message;
import com.furniro.MessageService.dto.event.AdminInboxEvent;
import com.furniro.MessageService.dto.res.CustomerProfile;
import com.furniro.MessageService.service.User.UserProfileGrpcClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminInboxEventFactory {
    private final UserProfileGrpcClient userProfileGrpcClient;

    public AdminInboxEvent fromConversation(String eventType, Conversation conversation, Message message) {
        CustomerProfile profile = userProfileGrpcClient.resolveBuyerProfile(conversation.getBuyerId());
        return AdminInboxEvent.fromConversation(eventType, conversation, message, profile);
    }
}
