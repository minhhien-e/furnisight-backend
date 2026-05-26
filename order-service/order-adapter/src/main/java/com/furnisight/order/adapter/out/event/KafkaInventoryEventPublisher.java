package com.furnisight.order.adapter.out.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.order.application.order.port.out.event.InventoryEventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaInventoryEventPublisher implements InventoryEventPublisherPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void publishStockReserveEvent(String orderCode, List<StockItem> items) {
        publishEvent("inventory-reserve", orderCode, items);
    }

    @Override
    public void publishStockReleaseEvent(String orderCode, List<StockItem> items) {
        publishEvent("inventory-release", orderCode, items);
    }

    private void publishEvent(String topic, String orderCode, List<StockItem> items) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("orderCode", orderCode);
            payload.put("items", items);
            
            String jsonPayload = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(topic, orderCode, jsonPayload);
            log.info("Published event to topic: {} for order: {}", topic, orderCode);
        } catch (Exception e) {
            log.error("Failed to publish event to topic: {}", topic, e);
        }
    }
}
