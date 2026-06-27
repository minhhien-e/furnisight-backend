package com.furniro.MessageService.service.Conversation;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.furniro.MessageService.database.entity.Conversation;
import com.furniro.MessageService.database.entity.Message;
import com.furniro.MessageService.database.repository.ConversationRepository;
import com.furniro.MessageService.database.repository.MessageRepository;
import com.furniro.MessageService.dto.API.AType;
import com.furniro.MessageService.dto.API.ApiType;
import com.furniro.MessageService.dto.MessageAttachment;
import com.furniro.MessageService.dto.req.Message.MessageReq;
import com.furniro.MessageService.exception.imp.MessageException;
import com.furniro.MessageService.util.error.MessageErrorCode;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageService {
    private static final ZoneId HO_CHI_MINH_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;

    @Transactional
    public ResponseEntity<AType> isRead(Integer messageID) {
        // 1. find message
        Message message = messageRepository.findById(messageID)
                .orElseThrow(() -> new MessageException(MessageErrorCode.MESSAGE_NOT_FOUND));

        // 2. mark as read
        message.setIsRead(true);
        
        // 3. save message
        messageRepository.save(message);

        // 4. return response
        return ResponseEntity.ok(ApiType.success(message));
    }

    public ResponseEntity<AType> getAllMessage(Integer conversationID, Integer page, Integer size) {
        return getAllMessage(conversationID, page, size, false);
        }

        public ResponseEntity<AType> getAllMessage(Integer conversationID, Integer page, Integer size, Boolean includeInternal) {
        // 1. find conversation
        Conversation conversation = conversationRepository.findById(conversationID)
                .orElseThrow(() -> new MessageException(MessageErrorCode.MESSAGE_NOT_FOUND));

        // 2. get all message by conversation
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messages = Boolean.TRUE.equals(includeInternal)
            ? messageRepository.findAllByConversation(conversation, pageable)
            : messageRepository.findByConversationAndIsInternalFalse(conversation, pageable);

        // 3. return response
        return ResponseEntity.ok(ApiType.success(messages));
    }

    public ResponseEntity<AType> searchMessages(
            Integer conversationID,
            String query,
            Integer page,
            Integer size,
            Boolean includeInternal) {
        conversationRepository.findById(conversationID)
                .orElseThrow(() -> new MessageException(MessageErrorCode.MESSAGE_NOT_FOUND));

        String normalizedQuery = query == null ? "" : query.trim();
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messages = normalizedQuery.isBlank()
                ? Page.empty(pageable)
                : messageRepository.searchByConversation(
                        conversationID,
                        normalizedQuery,
                        Boolean.TRUE.equals(includeInternal),
                        pageable);

        return ResponseEntity.ok(ApiType.success(messages));
    }

        @Transactional
        public ResponseEntity<AType> createInternalNote(MessageReq messageReq) {
        Conversation conversation = conversationRepository.findById(messageReq.getConversationId())
            .orElseThrow(() -> new MessageException(MessageErrorCode.MESSAGE_NOT_FOUND));
        List<MessageAttachment> attachments = normalizeAttachments(messageReq);
        MessageAttachment primaryAttachment = attachments.isEmpty() ? null : attachments.get(0);

        Message message = Message.builder()
            .conversation(conversation)
            .content(messageReq.getContent())
            .receiverId(conversation.getBuyerId())
            .senderId(messageReq.getSenderId())
            .type(messageReq.getMessageType() != null ? messageReq.getMessageType() : com.furniro.MessageService.util.enums.MessageType.TEXT)
            .fileId(messageReq.getFileId())
            .mediaId(primaryAttachment != null ? primaryAttachment.getMediaId() : messageReq.getMediaId())
            .attachmentUrl(primaryAttachment != null ? primaryAttachment.getUrl() : messageReq.getAttachmentUrl())
            .attachmentName(primaryAttachment != null ? primaryAttachment.getName() : messageReq.getAttachmentName())
            .attachmentType(primaryAttachment != null ? primaryAttachment.getType() : messageReq.getAttachmentType())
            .attachmentSize(primaryAttachment != null ? primaryAttachment.getSize() : messageReq.getAttachmentSize())
            .attachments(attachments)
            .isInternal(true)
            .build();

        conversation.setLastMessageAt(LocalDateTime.now(HO_CHI_MINH_ZONE));

        messageRepository.save(message);
        conversationRepository.save(conversation);

        return ResponseEntity.ok(ApiType.success(message));
        }

    @Transactional
    public Message createMessage(MessageReq messageReq) {
        // 1. find conversation
        Conversation conversation = conversationRepository.findById(messageReq.getConversationId())
                .orElseThrow(() -> new MessageException(MessageErrorCode.MESSAGE_NOT_FOUND));
        List<MessageAttachment> attachments = normalizeAttachments(messageReq);
        MessageAttachment primaryAttachment = attachments.isEmpty() ? null : attachments.get(0);

        // 2. create message
        Message message = Message.builder()
                .conversation(conversation)
                .content(messageReq.getContent())
            .receiverId(messageReq.getReceiverId() != null
                ? messageReq.getReceiverId()
                : (conversation.getStaffId() != null ? conversation.getStaffId() : conversation.getBuyerId()))
                .senderId(messageReq.getSenderId())
                .type(messageReq.getMessageType() != null ? messageReq.getMessageType() : com.furniro.MessageService.util.enums.MessageType.TEXT)
                .fileId(messageReq.getFileId())
                .mediaId(primaryAttachment != null ? primaryAttachment.getMediaId() : messageReq.getMediaId())
                .attachmentUrl(primaryAttachment != null ? primaryAttachment.getUrl() : messageReq.getAttachmentUrl())
                .attachmentName(primaryAttachment != null ? primaryAttachment.getName() : messageReq.getAttachmentName())
                .attachmentType(primaryAttachment != null ? primaryAttachment.getType() : messageReq.getAttachmentType())
                .attachmentSize(primaryAttachment != null ? primaryAttachment.getSize() : messageReq.getAttachmentSize())
                .attachments(attachments)
                .isInternal(Boolean.TRUE.equals(messageReq.getIsInternal()))
                .build();

        // 3. update conversation last message info
        LocalDateTime now = LocalDateTime.now(HO_CHI_MINH_ZONE);
        if (!Boolean.TRUE.equals(messageReq.getIsInternal())) {
            conversation.setLastMessageContent(message.getContent());
            
            if (messageReq.getSenderId().equals(conversation.getBuyerId())) {
                moveBackToInProgressWhenCustomerReplies(conversation);
            } else {
                moveToWaitingCustomerAfterAdminReply(conversation);
            }
        }
        conversation.setLastMessageAt(now);

        // 4. save message and conversation
        messageRepository.save(message);
        conversationRepository.save(conversation);

        // 5. return response
        return message;
    }

    private void moveBackToInProgressWhenCustomerReplies(Conversation conversation) {
        if (com.furniro.MessageService.util.enums.ConversationStatus.CLOSED.equals(conversation.getStatus())) {
            conversation.setStatus(com.furniro.MessageService.util.enums.ConversationStatus.OPEN);
            return;
        }

        if (com.furniro.MessageService.util.enums.ConversationStatus.WAITING_CUSTOMER.equals(conversation.getStatus())
                || com.furniro.MessageService.util.enums.ConversationStatus.RESOLVED.equals(conversation.getStatus())) {
            conversation.setStatus(com.furniro.MessageService.util.enums.ConversationStatus.IN_PROGRESS);
        }
    }

    private void moveToWaitingCustomerAfterAdminReply(Conversation conversation) {
        if (com.furniro.MessageService.util.enums.ConversationStatus.CLOSED.equals(conversation.getStatus())) {
            return;
        }
        conversation.setStatus(com.furniro.MessageService.util.enums.ConversationStatus.WAITING_CUSTOMER);
    }

    private List<MessageAttachment> normalizeAttachments(MessageReq messageReq) {
        if (messageReq.getAttachments() != null && !messageReq.getAttachments().isEmpty()) {
            return messageReq.getAttachments();
        }

        boolean hasLegacyAttachment = messageReq.getAttachmentUrl() != null
                || messageReq.getAttachmentName() != null
                || messageReq.getMediaId() != null
                || messageReq.getFileId() != null;
        if (!hasLegacyAttachment) {
            return List.of();
        }

        return List.of(MessageAttachment.builder()
                .mediaId(messageReq.getMediaId())
                .url(messageReq.getAttachmentUrl())
                .name(messageReq.getAttachmentName())
                .type(messageReq.getAttachmentType())
                .size(messageReq.getAttachmentSize())
                .isImage(com.furniro.MessageService.util.enums.MessageType.IMAGE.equals(messageReq.getMessageType())
                        || (messageReq.getAttachmentType() != null && messageReq.getAttachmentType().startsWith("image/")))
                .build());
    }
}
