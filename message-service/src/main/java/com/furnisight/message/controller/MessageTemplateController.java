package com.furnisight.message.controller;

import com.furnisight.message.dto.API.AType;
import com.furnisight.message.dto.req.MessageTemplateReq;
import com.furnisight.message.service.Conversation.MessageTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/message-templates")
@RequiredArgsConstructor
public class MessageTemplateController {

    private final MessageTemplateService templateService;

    @GetMapping
    public ResponseEntity<AType> getAllTemplates() {
        return templateService.getAllTemplates();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AType> getTemplateById(@PathVariable Integer id) {
        return templateService.getTemplateById(id);
    }

    @PostMapping
    public ResponseEntity<AType> createTemplate(@Valid @RequestBody MessageTemplateReq req) {
        return templateService.createTemplate(req);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AType> updateTemplate(@PathVariable Integer id, @Valid @RequestBody MessageTemplateReq req) {
        return templateService.updateTemplate(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<AType> deleteTemplate(@PathVariable Integer id) {
        return templateService.deleteTemplate(id);
    }
}
