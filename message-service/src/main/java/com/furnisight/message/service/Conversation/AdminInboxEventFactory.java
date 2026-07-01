package com.furnisight.message.service.Conversation;

import org.springframework.stereotype.Service;

import com.furnisight.message.database.entity.Conversation;
import com.furnisight.message.database.entity.Message;
import com.furnisight.message.dto.event.AdminInboxEvent;
import com.furnisight.message.dto.res.CustomerProfile;
import com.furnisight.message.service.User.UserProfileGrpcClient;

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
