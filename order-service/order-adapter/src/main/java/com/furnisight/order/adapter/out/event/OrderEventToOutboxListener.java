package com.furnisight.order.adapter.out.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.order.domain.entities.OutboxMessage;
import com.furnisight.order.domain.events.OrderCancelledEvent;
import com.furnisight.order.domain.events.OrderCreatedEvent;
import com.furnisight.order.domain.events.OrderPaidEvent;
import com.furnisight.order.domain.repository.OutboxMessageRepository;
import com.furnisight.order.domain.repository.reservation.StockReservationRepository;
import com.furnisight.order.domain.repository.order.OrderRepository;
import com.furnisight.order.domain.entities.reservation.StockReservation;
import com.furnisight.order.domain.entities.order.Order;
import com.furnisight.order.domain.entities.order.OrderItem;
import com.furnisight.order.application.user.port.out.UserEmailPort;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventToOutboxListener {

    private final OutboxMessageRepository outboxMessageRepository;
    private final StockReservationRepository stockReservationRepository;
    private final OrderRepository orderRepository;
    private final UserEmailPort userEmailPort;
    private final ObjectMapper objectMapper;
    private static final String AGGREGATE_TYPE = "Order";

    @SneakyThrows
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(OrderCreatedEvent event) {
        log.info("Handling OrderCreatedEvent to outbox for order: {}", event.getOrderCode());
        Order order = orderRepository.findByOrderCode(event.getOrderCode())
                .orElseThrow(() -> new IllegalStateException("Order not found for event: " + event.getOrderCode()));
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new IllegalStateException("Order has no items for event: " + event.getOrderCode());
        }

        List<InventoryStockItemPayload> stockItems = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            // Save stock reservation
            StockReservation reservation = StockReservation.builder()
                    .orderCode(order.getOrderCode())
                    .productId(UUID.fromString(item.getProductSnapshot().getProductId()))
                    .productVariantId(item.getProductSnapshot().getVariantId() != null ? UUID.fromString(item.getProductSnapshot().getVariantId()) : null)
                    .quantity(item.getQuantity())
                    .build();
            stockReservationRepository.save(reservation);

            stockItems.add(new InventoryStockItemPayload(
                    item.getProductSnapshot().getProductId(),
                    item.getProductSnapshot().getVariantId(),
                    item.getQuantity()));
        }

        InventoryStockPayload payload = new InventoryStockPayload(event.getOrderCode(), stockItems);

        outboxMessageRepository.save(new OutboxMessage(
                AGGREGATE_TYPE,
                event.getOrderCode(),
                "inventory-reserve",
                objectMapper.writeValueAsString(payload)
        ));

        List<OrderPlacedItemPayload> itemsList = order.getItems().stream()
                .map(item -> new OrderPlacedItemPayload(
                        item.getProductSnapshot().getProductName(),
                        item.getQuantity(),
                        item.getPrice()))
                .toList();
        OrderPlacedPayload orderPlacedPayload = new OrderPlacedPayload(
                order.getOrderCode(),
                order.getUserId() == null ? null : order.getUserId().toString(),
                event.getCustomerEmail(),
                order.getTotalAmount(),
                order.getCreatedAt() == null ? null : order.getCreatedAt().toString(),
                itemsList);

        outboxMessageRepository.save(new OutboxMessage(
                AGGREGATE_TYPE,
                event.getOrderCode(),
                "order-placed",
                objectMapper.writeValueAsString(orderPlacedPayload)
        ));
    }

    @SneakyThrows
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(OrderCancelledEvent event) {
        log.info("Handling OrderCancelledEvent to outbox for order: {}", event.getOrderCode());
        releaseStock(event.getOrderCode());
    }

    @SneakyThrows
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(OrderPaidEvent event) {
        Order order = orderRepository.findByOrderCode(event.getOrderCode())
                .orElseThrow(() -> new IllegalStateException("Order not found for event: " + event.getOrderCode()));

        OrderPaidPayload payload = new OrderPaidPayload(
                event.getOrderCode(),
                order.getUserId() == null ? null : order.getUserId().toString(),
                userEmailPort.getEmailByUserId(order.getUserId()),
                event.getPaidAmount(),
                event.getPaymentMethod());

        outboxMessageRepository.save(new OutboxMessage(
                AGGREGATE_TYPE,
                event.getOrderCode(),
                "order-paid",
                objectMapper.writeValueAsString(payload)
        ));
    }

    private void releaseStock(String orderCode) throws Exception {
        List<StockReservation> reservations = stockReservationRepository.findByOrderCode(orderCode);
        if (reservations != null && !reservations.isEmpty()) {
            List<InventoryStockItemPayload> stockItems = new ArrayList<>();
            for (StockReservation res : reservations) {
                stockItems.add(new InventoryStockItemPayload(
                        res.getProductId() == null ? null : res.getProductId().toString(),
                        res.getProductVariantId() == null ? null : res.getProductVariantId().toString(),
                        res.getQuantity()));
            }

            InventoryStockPayload payload = new InventoryStockPayload(orderCode, stockItems);

            outboxMessageRepository.save(new OutboxMessage(
                    AGGREGATE_TYPE,
                    orderCode,
                    "inventory-release",
                    objectMapper.writeValueAsString(payload)
            ));

            stockReservationRepository.deleteByOrderCode(orderCode);
        }
    }

    private record InventoryStockPayload(String orderCode, List<InventoryStockItemPayload> items) {
    }

    private record InventoryStockItemPayload(String productId, String variantId, Integer quantity) {
    }

    private record OrderPlacedPayload(
            String orderCode,
            String userId,
            String customerEmail,
            Double totalAmount,
            String createdAt,
            List<OrderPlacedItemPayload> items
    ) {
    }

    private record OrderPlacedItemPayload(String productName, Integer quantity, Double price) {
    }

    private record OrderPaidPayload(String orderCode, String userId, String customerEmail, Double paidAmount, String paymentMethod) {
    }
}
