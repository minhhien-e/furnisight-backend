package com.furnisight.notification.adapter.in.web.controller.rest;

import com.furnisight.notification.adapter.in.web.dto.inbox.SaveInboxMessageRequest;
import com.furnisight.notification.application.common.port.in.CurrentUserProvider;
import com.furnisight.notification.application.inbox.port.in.dto.command.DeleteInboxMessageCommand;
import com.furnisight.notification.application.inbox.port.in.dto.command.MarkAllInboxMessagesAsReadCommand;
import com.furnisight.notification.application.inbox.port.in.dto.command.MarkInboxMessageAsReadCommand;
import com.furnisight.notification.application.inbox.port.in.dto.command.SaveInboxMessageCommand;
import com.furnisight.notification.application.inbox.port.in.dto.projection.InboxMessageProjection;
import com.furnisight.notification.application.inbox.port.in.dto.query.GetInboxMessageQuery;
import com.furnisight.notification.application.inbox.port.in.usecase.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/inbox-messages")
@RequiredArgsConstructor
public class InboxMessageController {

    private final SaveInboxMessageUseCase saveInboxMessageUseCase;
    private final MarkInboxMessageAsReadUseCase markInboxMessageAsReadUseCase;
    private final MarkAllInboxMessagesAsReadUseCase markAllInboxMessagesAsReadUseCase;
    private final DeleteInboxMessageUseCase deleteInboxMessageUseCase;
    private final GetInboxMessageUseCase getInboxMessageUseCase;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    public ResponseEntity<InboxMessageProjection> saveMessage(@Valid @RequestBody SaveInboxMessageRequest request) {
        SaveInboxMessageCommand command = SaveInboxMessageCommand.builder()
            .userId(currentUserProvider.getCurrentUserId())
            .title(request.getTitle())
            .body(request.getBody())
            .image(request.getImage())
            .actionUrl(request.getActionUrl())
            .type(request.getType())
            .build();
        var savedMessage = saveInboxMessageUseCase.execute(command);
        return new ResponseEntity<>(savedMessage, HttpStatus.CREATED);
    }

    @PutMapping("/{messageId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID messageId) {
        markInboxMessageAsReadUseCase.execute(new MarkInboxMessageAsReadCommand(messageId));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        markAllInboxMessagesAsReadUseCase.execute(new MarkAllInboxMessagesAsReadCommand(currentUserProvider.getCurrentUserId()));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(
        @PathVariable UUID messageId,
        @RequestParam(defaultValue = "false") boolean isHardDelete) {
        deleteInboxMessageUseCase.execute(new DeleteInboxMessageCommand(messageId, isHardDelete));
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<InboxMessageProjection>> getAllMessages(
        @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam(value = "isDeleted", defaultValue = "false") boolean isDeleted,
        @RequestParam(value = "limit", defaultValue = "20") int limit
    ) {
        UUID userId = currentUserProvider.getCurrentUserId();

        if (startDate == null) {
            startDate = LocalDateTime.now().minusYears(5);
        }

        var request = GetInboxMessageQuery.builder()
            .userId(userId)
            .startDate(startDate)
            .isDeleted(isDeleted)
            .limit(limit)
            .build();

        var result = getInboxMessageUseCase.execute(request);

        return ResponseEntity.ok(result);
    }
}
