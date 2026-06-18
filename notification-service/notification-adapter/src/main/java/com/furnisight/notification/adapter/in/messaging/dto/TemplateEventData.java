package com.furnisight.notification.adapter.in.messaging.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.notification.domain.service.TemplateData;

public record TemplateEventData(JsonNode root) implements TemplateData {
    public static TemplateEventData from(ObjectMapper objectMapper, Object event) {
        return new TemplateEventData(objectMapper.valueToTree(event));
    }

    @Override
    public String resolve(String key) {
        JsonNode value = root.path(key);
        if (value.isMissingNode() || value.isNull()) {
            throw new IllegalArgumentException("Missing template variable: " + key);
        }
        if (value.isNumber()) {
            return value.decimalValue().stripTrailingZeros().toPlainString();
        }
        return value.isTextual() ? value.asText() : value.toString();
    }
}
