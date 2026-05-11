package com.furnisight.notification.adapter.in.web.controller.rest;

import com.furnisight.notification.adapter.in.web.dto.template.CreateNotificationTemplateRequest;
import com.furnisight.notification.adapter.in.web.dto.template.UpdateNotificationTemplateRequest;
import com.furnisight.notification.application.template.port.in.dto.projection.NotificationTemplateProjection;
import com.furnisight.notification.application.template.port.in.dto.command.CreateNotificationTemplateCommand;
import com.furnisight.notification.application.template.port.in.dto.command.DeleteNotificationTemplateCommand;
import com.furnisight.notification.application.template.port.in.dto.command.UpdateNotificationTemplateCommand;
import com.furnisight.notification.application.template.port.in.dto.query.FilterNotificationTemplateQuery;
import com.furnisight.notification.application.template.port.in.dto.query.FindNotificationTemplateByCodeQuery;
import com.furnisight.notification.application.template.port.in.usecase.*;
import com.furnisight.notification.domain.model.enums.NotificationChannel;
import com.furnisight.notification.domain.model.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("notification-templates")
@RequiredArgsConstructor
public class NotificationTemplateController {

    private final CreateNotificationTemplateUseCase createNotificationTemplateUseCase;
    private final DeleteNotificationTemplateUseCase deleteNotificationTemplateUseCase;
    private final UpdateNotificationTemplateUseCase updateNotificationTemplateUseCase;
    private final FilterNotificationTemplateUseCase filterNotificationTemplateUseCase;
    private final FindNotificationTemplateByCodeUseCase findNotificationTemplateByCodeUseCase;

    @PostMapping
    public ResponseEntity<NotificationTemplateProjection> createTemplate(@Valid @RequestBody CreateNotificationTemplateRequest request) {
        CreateNotificationTemplateCommand command = CreateNotificationTemplateCommand.builder()
                .code(request.getCode())
                .name(request.getName())
                .titleTemplate(request.getTitleTemplate())
                .bodyTemplate(request.getBodyTemplate())
                .type(request.getType())
                .channel(request.getChannel())
                .defaultImage(request.getDefaultImage())
                .defaultActionUrl(request.getDefaultActionUrl())
                .build();

        NotificationTemplateProjection createdTemplate = createNotificationTemplateUseCase.execute(command);
        return new ResponseEntity<>(createdTemplate, HttpStatus.CREATED);
    }

    @DeleteMapping("/{templateId}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable UUID templateId) {
        DeleteNotificationTemplateCommand command = new DeleteNotificationTemplateCommand(templateId);
        deleteNotificationTemplateUseCase.execute(command);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{templateId}")
    public ResponseEntity<NotificationTemplateProjection> updateTemplate(
            @PathVariable UUID templateId,
            @Valid @RequestBody UpdateNotificationTemplateRequest request) {
        UpdateNotificationTemplateCommand command = UpdateNotificationTemplateCommand.builder()
                .templateId(templateId)
                .name(request.getName())
                .titleTemplate(request.getTitleTemplate())
                .bodyTemplate(request.getBodyTemplate())
                .build();
        NotificationTemplateProjection updatedTemplate = updateNotificationTemplateUseCase.execute(command);
        return ResponseEntity.ok(updatedTemplate);
    }

    @GetMapping
    public ResponseEntity<List<NotificationTemplateProjection>> filterTemplates(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) NotificationChannel channel,
            @RequestParam(required = false) NotificationType type) {
        FilterNotificationTemplateQuery query = FilterNotificationTemplateQuery.builder()
                .name(name)
                .channel(channel)
                .type(type)
                .build();
        List<NotificationTemplateProjection> templates = filterNotificationTemplateUseCase.execute(query);
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<NotificationTemplateProjection> findTemplateByCode(@PathVariable String code) {
        FindNotificationTemplateByCodeQuery query = FindNotificationTemplateByCodeQuery.builder()
                .code(code)
                .build();
        NotificationTemplateProjection template = findNotificationTemplateByCodeUseCase.execute(query);
        return ResponseEntity.ok(template);
    }
}
