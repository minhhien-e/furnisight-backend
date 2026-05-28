package com.furniro.MessageService.service.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.furniro.MessageService.dto.req.Mail.MailActiveReq;
import com.furniro.MessageService.dto.req.Mail.MailOTPReq;
import com.furniro.MessageService.dto.req.Notify.NotificationReq;
import com.furniro.MessageService.service.Notification.NotificationService;
import com.furniro.MessageService.service.Other.MailService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumer {

    private final MailService mailService;
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    @Transactional
    @KafkaListener(
            topics = "auth.send.active", 
            groupId = "message-service-group", 
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onUserCreated(Map<String, Object> event) {
        try {

            log.info("Received auth.send.active event: {}", event);

            MailActiveReq req = objectMapper.convertValue(event, MailActiveReq.class);
            
            String fullName = req.getFirstName() + " " + req.getLastName();
            
            mailService.sendMailActive(req.getEmail(), fullName, req.getAccountID());

        } catch (Exception e) {
            log.error("Failed to process auth.send.active event: {}", e.getMessage());
        }

    }

    @Transactional
    @KafkaListener(
        topics = "auth.send.otp", 
        groupId = "message-service-group", 
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMailOTP(Map<String, Object> event) {
        try {

            log.info("Received auth.send.otp event: {}", event);

            MailOTPReq req = objectMapper.convertValue(event, MailOTPReq.class);
            mailService.sendMailOTP(req.getEmail(), req.getUserName(), req.getOtp());

        } catch (Exception e) {
            log.error("Failed to process auth.send.otp event: {}", e.getMessage());
        }

    }

    @Transactional
    @KafkaListener(
        topics = "notification.created", 
        groupId = "message-service-group", 
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onNotificationCreated(Map<String, Object> message) {

        try {

            log.info("Received notification.created event: {}", message);

            NotificationReq req = objectMapper.convertValue(message, NotificationReq.class);

            notificationService.createNotification(req);
            
            messagingTemplate.convertAndSend("/topic/notifications/" + req.getUserID(), req);

        } catch (Exception e) {

            log.error("Failed to process notification.created event: {}", e.getMessage());
        }
    }

}