package com.furnisight.message.service.Conversation;

import com.furnisight.message.database.entity.MessageTemplate;
import com.furnisight.message.database.repository.MessageTemplateRepository;
import com.furnisight.message.dto.API.AType;
import com.furnisight.message.dto.API.ApiType;
import com.furnisight.message.dto.req.MessageTemplateReq;
import com.furnisight.message.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageTemplateService {

    private final MessageTemplateRepository templateRepository;

    public ResponseEntity<AType> getAllTemplates() {
        List<MessageTemplate> templates = templateRepository.findAll();
        return ResponseEntity.ok(ApiType.success(templates));
    }

    public ResponseEntity<AType> getTemplateById(Integer id) {
        MessageTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new BaseException(404, "Template not found"));
        return ResponseEntity.ok(ApiType.success(template));
    }

    public ResponseEntity<AType> createTemplate(MessageTemplateReq req) {
        MessageTemplate template = MessageTemplate.builder()
                .title(req.getTitle())
                .content(req.getContent())
                .category(req.getCategory() != null ? req.getCategory() : "GREETING")
                .active(req.getActive() != null ? req.getActive() : true)
                .build();
        MessageTemplate saved = templateRepository.save(template);
        return ResponseEntity.ok(ApiType.success(saved));
    }

    public ResponseEntity<AType> updateTemplate(Integer id, MessageTemplateReq req) {
        MessageTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new BaseException(404, "Template not found"));

        if (req.getTitle() != null) template.setTitle(req.getTitle());
        if (req.getContent() != null) template.setContent(req.getContent());
        if (req.getCategory() != null) template.setCategory(req.getCategory());
        if (req.getActive() != null) template.setActive(req.getActive());

        MessageTemplate updated = templateRepository.save(template);
        return ResponseEntity.ok(ApiType.success(updated));
    }

    public ResponseEntity<AType> deleteTemplate(Integer id) {
        MessageTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new BaseException(404, "Template not found"));
        templateRepository.delete(template);
        return ResponseEntity.ok(ApiType.success(null));
    }
}
