package com.furnisight.message.service.Conversation;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.furnisight.message.database.entity.Conversation;
import com.furnisight.message.database.entity.Message;
import com.furnisight.message.database.repository.ConversationRepository;
import com.furnisight.message.database.repository.MessageRepository;
import com.furnisight.message.dto.API.AType;
import com.furnisight.message.dto.API.ApiType;
import com.furnisight.message.dto.MessageAttachment;
import com.furnisight.message.dto.event.UploadActiveEvent;
import com.furnisight.message.dto.req.Message.ConversationReq;
import com.furnisight.message.dto.res.ConversationResponse;
import com.furnisight.message.dto.res.CustomerProfile;
import com.furnisight.message.domain.exceptions.ErrorCode;
import com.furnisight.message.domain.exceptions.NotFoundException;
import com.furnisight.message.service.kafka.KafkaProducer;
import com.furnisight.message.service.User.UserProfileGrpcClient;
import com.furnisight.message.util.enums.ConversationChannel;
import com.furnisight.message.util.enums.ConversationPriority;
import com.furnisight.message.util.enums.ConversationStatus;
import com.furnisight.message.util.enums.MessageType;


import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConversationService {

    private static final ZoneId HO_CHI_MINH_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
        
    final ConversationRepository conversationRepository;
    final MessageRepository messageRepository;
    final KafkaProducer kafkaProducer;
    final UserProfileGrpcClient userProfileGrpcClient;

    @Transactional
    public ResponseEntity<AType> createConversation(ConversationReq req) {
        ConversationChannel channel = req.getChannel() != null ? req.getChannel() : ConversationChannel.SUPPORT;

        Conversation existingConversation = conversationRepository
            .findTopByBuyerIdOrderByUpdatedAtDesc(req.getBuyerId());
        List<MessageAttachment> attachments = normalizeAttachments(req);
        MessageAttachment primaryAttachment = attachments.isEmpty() ? null : attachments.get(0);

        if (existingConversation != null) {
            Message message = Message.builder()
                .conversation(existingConversation)
                .senderId(req.getBuyerId())
                .receiverId(req.getStaffId() != null ? req.getStaffId()
                    : (existingConversation.getStaffId() != null ? existingConversation.getStaffId() : 1))
                .content(req.getMessage())
                .type(req.getMessageType())
                .fileId(req.getFileId())
                .mediaId(primaryAttachment != null ? primaryAttachment.getMediaId() : req.getMediaId())
                .attachmentUrl(primaryAttachment != null ? primaryAttachment.getUrl() : req.getAttachmentUrl())
                .attachmentName(primaryAttachment != null ? primaryAttachment.getName() : req.getAttachmentName())
                .attachmentType(primaryAttachment != null ? primaryAttachment.getType() : req.getAttachmentType())
                .attachmentSize(primaryAttachment != null ? primaryAttachment.getSize() : req.getAttachmentSize())
                .attachments(attachments)
                .build();

            if (MessageType.IMAGE.equals(req.getMessageType())) {
                kafkaProducer.send("upload.active", new UploadActiveEvent(req.getFileId()));
            }

            existingConversation.setLastMessageContent(req.getMessage());
            existingConversation.setLastMessageAt(LocalDateTime.now(HO_CHI_MINH_ZONE));
            moveBackToInProgressWhenCustomerReplies(existingConversation);
            if (existingConversation.getStaffId() == null && req.getStaffId() != null) {
                existingConversation.setStaffId(req.getStaffId());
            }

            conversationRepository.save(existingConversation);
            messageRepository.save(message);

            return ResponseEntity.ok(ApiType.success(existingConversation));
        }

        // 1. create conversation
        Conversation conversation = Conversation.builder()
                .buyerId(req.getBuyerId())
                .staffId(req.getStaffId())
            .channel(channel)
                .lastMessageContent(req.getMessage())
                .status(ConversationStatus.OPEN)
                .build();

        conversationRepository.save(conversation);

        // 2. create message
        Message message = Message.builder()
                .conversation(conversation)
                .senderId(req.getBuyerId())
                .receiverId(req.getStaffId() != null ? req.getStaffId() : 1)
                .content(req.getMessage())
                .type(req.getMessageType())
                .fileId(req.getFileId())
                .mediaId(primaryAttachment != null ? primaryAttachment.getMediaId() : req.getMediaId())
                .attachmentUrl(primaryAttachment != null ? primaryAttachment.getUrl() : req.getAttachmentUrl())
                .attachmentName(primaryAttachment != null ? primaryAttachment.getName() : req.getAttachmentName())
                .attachmentType(primaryAttachment != null ? primaryAttachment.getType() : req.getAttachmentType())
                .attachmentSize(primaryAttachment != null ? primaryAttachment.getSize() : req.getAttachmentSize())
                .attachments(attachments)
                .build();

        // 3 if message type is Image , send kafka active image
        if (MessageType.IMAGE.equals(req.getMessageType())) {
            // send kafka active image
            kafkaProducer.send("upload.active", new UploadActiveEvent(req.getFileId()));

        }
        
        messageRepository.save(message);

        return ResponseEntity.ok(ApiType.success(conversation));
    }

    public ResponseEntity<AType> getAdminInbox(
            ConversationChannel channel,
            List<ConversationStatus> statuses,
            ConversationPriority priority,
            Integer assignedAdminId,
            int page,
            int size) {

        List<Conversation> conversations = conversationRepository.findAll();

        List<Conversation> latestByBuyer = collapseLatestByBuyer(conversations);

        List<Conversation> filtered = latestByBuyer.stream()
                .filter(conversation -> channel == null || conversation.getChannel() == channel)
                .filter(conversation -> statuses == null || statuses.isEmpty() || statuses.contains(conversation.getStatus()))
                .filter(conversation -> priority == null || conversation.getPriority() == priority)
                .filter(conversation -> assignedAdminId == null || assignedAdminId.equals(conversation.getAssignedAdminId()))
                .sorted(java.util.Comparator.comparing(Conversation::getUpdatedAt, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())).reversed())
                .collect(Collectors.toList());

        int start = Math.min(page * size, filtered.size());
        int end = Math.min((page + 1) * size, filtered.size());
        List<Conversation> pagedList = filtered.subList(start, end);

        Map<Integer, CustomerProfile> profilesByBuyerId = userProfileGrpcClient.resolveBuyerProfiles(
                pagedList.stream().map(Conversation::getBuyerId).collect(Collectors.toSet()));
        List<ConversationResponse> responseList = pagedList.stream()
                .map(conversation -> ConversationResponse.from(conversation, profilesByBuyerId.get(conversation.getBuyerId())))
                .toList();

        org.springframework.data.domain.Page<ConversationResponse> pageResult = new org.springframework.data.domain.PageImpl<>(
                responseList,
                org.springframework.data.domain.PageRequest.of(page, size),
                filtered.size()
        );

        return ResponseEntity.ok(ApiType.success(pageResult));
    }

    @Transactional
    public ResponseEntity<AType> getAllConversation(Integer userId) {
        List<Conversation> conversations = conversationRepository.findByBuyerIdOrStaffId(userId, userId);

        // Removed auto-creation logic

        return ResponseEntity.ok(ApiType.success(conversations));
    }

    public ResponseEntity<AType> getConversationById(int id) {
        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MESSAGE_NOT_FOUND));

        return ResponseEntity.ok(ApiType.success(
                ConversationResponse.from(conversation, userProfileGrpcClient.resolveBuyerProfile(conversation.getBuyerId()))));
    }

    public ResponseEntity<AType> getConversationByChannel(ConversationChannel channel) {
        return ResponseEntity.ok(ApiType.success(conversationRepository.findByChannel(channel)));
    }

    @Transactional
    public ResponseEntity<AType> updateConversationStatus(Integer conversationId, ConversationStatus status) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MESSAGE_NOT_FOUND));

        applyStatus(conversation, status);
        conversationRepository.save(conversation);
        return ResponseEntity.ok(ApiType.success(conversation));
    }

    @Transactional
    public ResponseEntity<AType> updateConversationPriority(Integer conversationId, ConversationPriority priority) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MESSAGE_NOT_FOUND));

        conversation.setPriority(priority);
        conversationRepository.save(conversation);
        return ResponseEntity.ok(ApiType.success(conversation));
    }

    @Transactional
    public ResponseEntity<AType> assignConversation(Integer conversationId, Integer adminId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MESSAGE_NOT_FOUND));

        conversation.setAssignedAdminId(adminId);
        conversation.setStaffId(adminId);
        conversation.setStatus(ConversationStatus.ASSIGNED);

        conversationRepository.save(conversation);
        return ResponseEntity.ok(ApiType.success(conversation));
    }

    @Transactional
    public ResponseEntity<AType> closeConversation(Integer conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MESSAGE_NOT_FOUND));

        applyStatus(conversation, ConversationStatus.CLOSED);
        conversationRepository.save(conversation);

        return ResponseEntity.ok(ApiType.success(conversation));
    }

    private void moveBackToInProgressWhenCustomerReplies(Conversation conversation) {
        if (ConversationStatus.CLOSED.equals(conversation.getStatus())) {
            conversation.setStatus(ConversationStatus.OPEN);
            return;
        }

        if (ConversationStatus.WAITING_CUSTOMER.equals(conversation.getStatus())
                || ConversationStatus.RESOLVED.equals(conversation.getStatus())) {
            conversation.setStatus(ConversationStatus.IN_PROGRESS);
        }
    }

    private void applyStatus(Conversation conversation, ConversationStatus status) {
        conversation.setStatus(status);
        if (ConversationStatus.CLOSED.equals(status)) {
            conversation.setClosedAt(LocalDateTime.now(HO_CHI_MINH_ZONE));
        }
    }

    private List<Conversation> collapseLatestByBuyer(List<Conversation> conversations) {
        Map<String, Conversation> latestByKey = new LinkedHashMap<>();

        conversations.stream()
                .sorted(java.util.Comparator.comparing(
                        Conversation::getUpdatedAt,
                        java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())
                ).reversed())
                .forEach(conversation -> {
                    String key = String.valueOf(conversation.getBuyerId());
                    latestByKey.putIfAbsent(key, conversation);
                });

        return latestByKey.values().stream().toList();
    }

    private List<MessageAttachment> normalizeAttachments(ConversationReq req) {
        if (req.getAttachments() != null && !req.getAttachments().isEmpty()) {
            return req.getAttachments();
        }

        boolean hasLegacyAttachment = req.getAttachmentUrl() != null
                || req.getAttachmentName() != null
                || req.getMediaId() != null
                || req.getFileId() != null;
        if (!hasLegacyAttachment) {
            return List.of();
        }

        return List.of(MessageAttachment.builder()
                .mediaId(req.getMediaId())
                .url(req.getAttachmentUrl())
                .name(req.getAttachmentName())
                .type(req.getAttachmentType())
                .size(req.getAttachmentSize())
                .isImage(MessageType.IMAGE.equals(req.getMessageType())
                        || (req.getAttachmentType() != null && req.getAttachmentType().startsWith("image/")))
                .build());
    }
}
