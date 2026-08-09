package com.furnisight.admin.message.application;

import com.furnisight.admin.message.infrastructure.MessageTemplateClient;
import com.furnisight.admin.message.web.dto.request.UpsertMessageTemplateRequest;
import com.furnisight.admin.message.web.dto.response.MessageTemplateResponse;
import com.furnisight.admin.shared.web.ActionResultResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageTemplateService {

    private final MessageTemplateClient messageTemplateClient;

    public List<MessageTemplateResponse> getTemplates() {
        return messageTemplateClient.getTemplates();
    }

    public MessageTemplateResponse getTemplateById(Integer id) {
        return messageTemplateClient.getTemplateById(id);
    }

    public MessageTemplateResponse createTemplate(UpsertMessageTemplateRequest request) {
        return messageTemplateClient.createTemplate(request);
    }

    public MessageTemplateResponse updateTemplate(Integer id, UpsertMessageTemplateRequest request) {
        return messageTemplateClient.updateTemplate(id, request);
    }

    public ActionResultResponse deleteTemplate(Integer id) {
        return messageTemplateClient.deleteTemplate(id);
    }
}
