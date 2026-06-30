package com.furnisight.message.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import com.furnisight.message.database.entity.Message;
import com.furnisight.message.dto.req.Message.MessageReq;
import com.furnisight.message.service.Conversation.AdminInboxEventFactory;
import com.furnisight.message.service.Conversation.MessageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ChatController {
    private static final String ADMIN_INBOX_TOPIC = "/topic/admin/inbox";
    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;
    private final AdminInboxEventFactory adminInboxEventFactory;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload MessageReq messageReq) {
        // 1. create message
        Message message = messageService.createMessage(messageReq);

        // 2. create topic
        String topic = Boolean.TRUE.equals(messageReq.getIsInternal())
            ? "/topic/conversation/" + messageReq.getConversationId() + "/internal"
            : "/topic/conversation/" + messageReq.getConversationId();
                
        // 3. check message created
        if (message != null) {
            messagingTemplate.convertAndSend(topic, message);
            if (!Boolean.TRUE.equals(messageReq.getIsInternal())) {
                messagingTemplate.convertAndSend(
                    ADMIN_INBOX_TOPIC,
                    adminInboxEventFactory.fromConversation("MESSAGE_CREATED", message.getConversation(), message));
            }
            log.info("Message sent successfully");
        } else {
            log.error("Message not created");
        }
    }
}
