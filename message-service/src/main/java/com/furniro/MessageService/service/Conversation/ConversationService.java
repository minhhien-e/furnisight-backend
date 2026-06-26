package com.furniro.MessageService.service.Conversation;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.furniro.MessageService.database.entity.Conversation;
import com.furniro.MessageService.database.entity.Message;
import com.furniro.MessageService.database.repository.ConversationRepository;
import com.furniro.MessageService.database.repository.MessageRepository;
import com.furniro.MessageService.dto.API.AType;
import com.furniro.MessageService.dto.API.ApiType;
import com.furniro.MessageService.dto.event.UploadActiveEvent;
import com.furniro.MessageService.dto.req.Message.ConversationReq;
import com.furniro.MessageService.dto.res.ConversationResponse;
import com.furniro.MessageService.dto.res.CustomerProfile;
import com.furniro.MessageService.exception.imp.MessageException;
import com.furniro.MessageService.service.kafka.KafkaProducer;
import com.furniro.MessageService.service.User.UserProfileGrpcClient;
import com.furniro.MessageService.util.enums.ConversationChannel;
import com.furniro.MessageService.util.enums.ConversationPriority;
import com.furniro.MessageService.util.enums.ConversationStatus;
import com.furniro.MessageService.util.enums.MessageType;
import com.furniro.MessageService.util.error.MessageErrorCode;

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
            .findTopByBuyerIdAndChannelOrderByUpdatedAtDesc(req.getBuyerId(), channel);

        if (existingConversation != null) {
            Message message = Message.builder()
                .conversation(existingConversation)
                .senderId(req.getBuyerId())
                .receiverId(req.getStaffId() != null ? req.getStaffId()
                    : (existingConversation.getStaffId() != null ? existingConversation.getStaffId() : 1))
                .content(req.getMessage())
                .type(req.getMessageType())
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

        List<Conversation> latestByBuyerAndChannel = collapseLatestByBuyerAndChannel(conversations);

        List<Conversation> filtered = latestByBuyerAndChannel.stream()
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
                .orElseThrow(() -> new MessageException(MessageErrorCode.MESSAGE_NOT_FOUND));

        return ResponseEntity.ok(ApiType.success(
                ConversationResponse.from(conversation, userProfileGrpcClient.resolveBuyerProfile(conversation.getBuyerId()))));
    }

    public ResponseEntity<AType> getConversationByChannel(ConversationChannel channel) {
        return ResponseEntity.ok(ApiType.success(conversationRepository.findByChannel(channel)));
    }

    @Transactional
    public ResponseEntity<AType> updateConversationStatus(Integer conversationId, ConversationStatus status) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new MessageException(MessageErrorCode.MESSAGE_NOT_FOUND));

        applyStatus(conversation, status);
        conversationRepository.save(conversation);
        return ResponseEntity.ok(ApiType.success(conversation));
    }

    @Transactional
    public ResponseEntity<AType> updateConversationPriority(Integer conversationId, ConversationPriority priority) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new MessageException(MessageErrorCode.MESSAGE_NOT_FOUND));

        conversation.setPriority(priority);
        conversationRepository.save(conversation);
        return ResponseEntity.ok(ApiType.success(conversation));
    }

    @Transactional
    public ResponseEntity<AType> assignConversation(Integer conversationId, Integer adminId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new MessageException(MessageErrorCode.MESSAGE_NOT_FOUND));

        conversation.setAssignedAdminId(adminId);
        conversation.setStaffId(adminId);
        conversation.setStatus(ConversationStatus.ASSIGNED);

        conversationRepository.save(conversation);
        return ResponseEntity.ok(ApiType.success(conversation));
    }

    @Transactional
    public ResponseEntity<AType> closeConversation(Integer conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new MessageException(MessageErrorCode.MESSAGE_NOT_FOUND));

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

    private List<Conversation> collapseLatestByBuyerAndChannel(List<Conversation> conversations) {
        Map<String, Conversation> latestByKey = new LinkedHashMap<>();

        conversations.stream()
                .sorted(java.util.Comparator.comparing(
                        Conversation::getUpdatedAt,
                        java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())
                ).reversed())
                .forEach(conversation -> {
                    String key = conversation.getBuyerId() + ":" + conversation.getChannel();
                    latestByKey.putIfAbsent(key, conversation);
                });

        return latestByKey.values().stream().toList();
    }
}
