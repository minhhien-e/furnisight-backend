package com.furnisight.catalog.infrastructure.event.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.catalog.application.product.dto.command.UpdateInventoryCommand;
import com.furnisight.catalog.application.product.port.in.usecase.ReleaseInventoryUseCase;
import com.furnisight.catalog.infrastructure.event.dto.InventoryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryReleaseConsumer {
    private static final String TOPIC = "inventory-release";

    private final ReleaseInventoryUseCase releaseInventoryUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = TOPIC)
    public void handle(String payload) {
        try {
            InventoryEvent event = objectMapper.readValue(payload, InventoryEvent.class);
            
            UpdateInventoryCommand command = UpdateInventoryCommand.builder()
                    .orderCode(event.orderCode())
                    .items(event.items().stream()
                            .map(item -> UpdateInventoryCommand.StockItem.builder()
                                    .productId(item.productId())
                                    .variantId(item.variantId())
                                    .quantity(item.quantity())
                                    .build())
                            .collect(Collectors.toList()))
                    .build();

            releaseInventoryUseCase.execute(command);
            log.info("Successfully released inventory for order: {}", event.orderCode());
        } catch (Exception e) {
            log.error("Failed to process inventory-release event", e);
        }
    }
}
