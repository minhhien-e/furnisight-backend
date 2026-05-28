package com.furniro.MessageService.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.furniro.MessageService.dto.API.AType;
import com.furniro.MessageService.dto.req.Message.MessageReq;
import com.furniro.MessageService.service.Conversation.MessageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;

    @GetMapping
    public ResponseEntity<AType> getMessages(
            @RequestParam Integer conversationID,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "false") Boolean includeInternal) {
        return messageService.getAllMessage(conversationID, page, size, includeInternal);
    }

    @PatchMapping("/{messageID}/read")
    public ResponseEntity<AType> markAsRead(@PathVariable Integer messageID) {
        return messageService.isRead(messageID);
    }

    @PostMapping("/{conversationID}/internal-note")
    public ResponseEntity<AType> createInternalNote(
            @PathVariable Integer conversationID,
            @RequestBody MessageReq req) {
        req.setConversationId(conversationID);
        return messageService.createInternalNote(req);
    }

}
