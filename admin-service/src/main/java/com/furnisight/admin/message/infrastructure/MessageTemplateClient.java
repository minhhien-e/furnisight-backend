package com.furnisight.admin.message.infrastructure;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.admin.message.web.dto.request.UpsertMessageTemplateRequest;
import com.furnisight.admin.message.web.dto.response.MessageTemplateResponse;
import com.furnisight.admin.shared.web.ActionResultResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageTemplateClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${message.service.http-url:http://message-service:8080}")
    private String messageServiceUrl;

    public List<MessageTemplateResponse> getTemplates() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    messageServiceUrl + "/message-templates", String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode data = root.path("data");
            return objectMapper.convertValue(data, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Failed to get message templates", e);
            return Collections.emptyList();
        }
    }

    public MessageTemplateResponse getTemplateById(Integer id) {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    messageServiceUrl + "/message-templates/" + id, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode data = root.path("data");
            return objectMapper.convertValue(data, MessageTemplateResponse.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException("Không tìm thấy mẫu tin nhắn với ID: " + id);
        } catch (Exception e) {
            log.error("Failed to get message template by id={}", id, e);
            throw new RuntimeException("Không thể lấy thông tin mẫu tin nhắn");
        }
    }

    public MessageTemplateResponse createTemplate(UpsertMessageTemplateRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<UpsertMessageTemplateRequest> entity = new HttpEntity<>(request, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    messageServiceUrl + "/message-templates", entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode data = root.path("data");
            return objectMapper.convertValue(data, MessageTemplateResponse.class);
        } catch (Exception e) {
            log.error("Failed to create message template", e);
            throw new RuntimeException("Không thể tạo mẫu tin nhắn: " + e.getMessage());
        }
    }

    public MessageTemplateResponse updateTemplate(Integer id, UpsertMessageTemplateRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<UpsertMessageTemplateRequest> entity = new HttpEntity<>(request, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    messageServiceUrl + "/message-templates/" + id,
                    HttpMethod.PUT, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode data = root.path("data");
            return objectMapper.convertValue(data, MessageTemplateResponse.class);
        } catch (Exception e) {
            log.error("Failed to update message template id={}", id, e);
            throw new RuntimeException("Không thể cập nhật mẫu tin nhắn: " + e.getMessage());
        }
    }

    public ActionResultResponse deleteTemplate(Integer id) {
        try {
            restTemplate.delete(messageServiceUrl + "/message-templates/" + id);
            return new ActionResultResponse(true, "Mẫu tin nhắn đã được xóa thành công");
        } catch (Exception e) {
            log.error("Failed to delete message template id={}", id, e);
            return new ActionResultResponse(false, "Không thể xóa mẫu tin nhắn: " + e.getMessage());
        }
    }
}
