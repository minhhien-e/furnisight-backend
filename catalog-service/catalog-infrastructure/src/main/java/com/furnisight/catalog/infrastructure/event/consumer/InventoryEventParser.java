package com.furnisight.catalog.infrastructure.event.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.catalog.infrastructure.event.dto.InventoryEvent;

final class InventoryEventParser {

    private InventoryEventParser() {
    }

    static InventoryEvent parse(ObjectMapper objectMapper, String payload) throws JsonProcessingException {
        JsonNode node = objectMapper.readTree(payload);
        if (node != null && node.isTextual()) {
            node = objectMapper.readTree(node.asText());
        }
        return objectMapper.treeToValue(node, InventoryEvent.class);
    }
}
