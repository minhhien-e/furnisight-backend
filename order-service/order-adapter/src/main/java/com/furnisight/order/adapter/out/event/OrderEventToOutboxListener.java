package com.furnisight.order.adapter.out.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.order.domain.entities.OutboxMessage;
import com.furnisight.order.domain.events.OrderCancelledEvent;
import com.furnisight.order.domain.events.OrderCreatedEvent;
import com.furnisight.order.domain.events.OrderPaymentFailedEvent;
import com.furnisight.order.domain.repository.OutboxMessageRepository;
import com.furnisight.order.domain.repository.reservation.StockReservationRepository;
import com.furnisight.order.domain.repository.order.OrderRepository;
import com.furnisight.order.domain.entities.reservation.StockReservation;
import com.furnisight.order.domain.entities.order.Order;
import com.furnisight.order.domain.entities.order.OrderItem;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventToOutboxListener {

    private final OutboxMessageRepository outboxMessageRepository;
    private final StockReservationRepository stockReservationRepository;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;
    private static final String AGGREGATE_TYPE = "Order";

    @SneakyThrows
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(OrderCreatedEvent event) {
        log.info("Handling OrderCreatedEvent to outbox for order: {}", event.getOrderCode());
        Order order = orderRepository.findByOrderCode(event.getOrderCode()).orElse(null);
        if (order == null || order.getItems() == null || order.getItems().isEmpty()) return;

        List<Map<String, Object>> stockItems = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            // Save stock reservation
            StockReservation reservation = StockReservation.builder()
                    .orderCode(order.getOrderCode())
                    .productId(UUID.fromString(item.getProductSnapshot().getProductId()))
                    .productVariantId(item.getProductSnapshot().getVariantId() != null ? UUID.fromString(item.getProductSnapshot().getVariantId()) : null)
                    .quantity(item.getQuantity())
                    .build();
            stockReservationRepository.save(reservation);

            Map<String, Object> stockItem = new HashMap<>();
            stockItem.put("productId", item.getProductSnapshot().getProductId());
            stockItem.put("variantId", item.getProductSnapshot().getVariantId());
            stockItem.put("quantity", item.getQuantity());
            stockItems.add(stockItem);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("orderCode", event.getOrderCode());
        payload.put("items", stockItems);

        outboxMessageRepository.save(new OutboxMessage(
                AGGREGATE_TYPE,
                event.getOrderCode(),
                "inventory-reserve",
                objectMapper.writeValueAsString(payload)
        ));
    }

    @SneakyThrows
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(OrderPaymentFailedEvent event) {
        log.info("Handling OrderPaymentFailedEvent to outbox for order: {}", event.getOrderCode());
        releaseStock(event.getOrderCode());
    }

    @SneakyThrows
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(OrderCancelledEvent event) {
        log.info("Handling OrderCancelledEvent to outbox for order: {}", event.getOrderCode());
        releaseStock(event.getOrderCode());
    }

    private void releaseStock(String orderCode) throws Exception {
        List<StockReservation> reservations = stockReservationRepository.findByOrderCode(orderCode);
        if (reservations != null && !reservations.isEmpty()) {
            List<Map<String, Object>> stockItems = new ArrayList<>();
            for (StockReservation res : reservations) {
                Map<String, Object> stockItem = new HashMap<>();
                stockItem.put("productId", res.getProductId());
                stockItem.put("variantId", res.getProductVariantId());
                stockItem.put("quantity", res.getQuantity());
                stockItems.add(stockItem);
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("orderCode", orderCode);
            payload.put("items", stockItems);

            outboxMessageRepository.save(new OutboxMessage(
                    AGGREGATE_TYPE,
                    orderCode,
                    "inventory-release",
                    objectMapper.writeValueAsString(payload)
            ));

            stockReservationRepository.deleteByOrderCode(orderCode);
        }
    }
}
