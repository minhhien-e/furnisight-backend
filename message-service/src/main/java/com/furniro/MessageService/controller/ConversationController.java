package com.furniro.MessageService.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.furniro.MessageService.dto.API.AType;
import com.furniro.MessageService.dto.req.Message.ConversationReq;
import com.furniro.MessageService.exception.BaseException;
import com.furniro.MessageService.util.enums.ConversationPriority;
import com.furniro.MessageService.util.enums.ConversationChannel;
import com.furniro.MessageService.util.enums.ConversationStatus;

import jakarta.validation.Valid;
import com.furniro.MessageService.service.Conversation.ConversationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/conversation")
@RequiredArgsConstructor
public class ConversationController {
    private final ConversationService conversationService;

    @PostMapping("/create")
    public ResponseEntity<AType> createConversation(@Valid @RequestBody ConversationReq req) {
        return conversationService.createConversation(req);
    }

    @GetMapping("/all/{userId}")
    public ResponseEntity<AType> getAllConversation(@PathVariable Integer userId) {
        return conversationService.getAllConversation(userId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AType> getConversationById(@PathVariable int id) {
        return conversationService.getConversationById(id);
    }

    @GetMapping("/channel/{channel}")
    public ResponseEntity<AType> getConversationByChannel(@PathVariable ConversationChannel channel) {
        return conversationService.getConversationByChannel(channel);
    }

    @GetMapping("/admin/inbox")
    public ResponseEntity<AType> getAdminInbox(
            @RequestParam(required = false) ConversationChannel channel,
            @RequestParam(required = false) ConversationStatus status,
            @RequestParam(required = false) ConversationPriority priority,
            @RequestParam(required = false) Integer assignedAdminId,
            @RequestParam(defaultValue = "false") Boolean unreadOnly) {
        return conversationService.getAdminInbox(channel, status, priority, assignedAdminId, unreadOnly);
    }

    @PatchMapping("/{id}/assign/{adminId}")
    public ResponseEntity<AType> assignConversation(
            @PathVariable Integer id,
            @PathVariable Integer adminId) {
        return conversationService.assignConversation(id, adminId);
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<AType> closeConversation(@PathVariable Integer id) {
        return conversationService.closeConversation(id);
    }

    @PatchMapping("/{id}/status/{status}")
    public ResponseEntity<AType> updateConversationStatus(
            @PathVariable Integer id,
            @PathVariable String status) {
        return conversationService.updateConversationStatus(id, parseEnum(status, ConversationStatus.class, "status"));
    }

    @PatchMapping("/{id}/priority/{priority}")
    public ResponseEntity<AType> updateConversationPriority(
            @PathVariable Integer id,
            @PathVariable String priority) {
        return conversationService.updateConversationPriority(id, parseEnum(priority, ConversationPriority.class, "priority"));
    }

    private <T extends Enum<T>> T parseEnum(String value, Class<T> enumClass, String fieldName) {
        try {
            return Enum.valueOf(enumClass, value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BaseException(400, "Invalid " + fieldName + " value: " + value);
        }
    }
}
