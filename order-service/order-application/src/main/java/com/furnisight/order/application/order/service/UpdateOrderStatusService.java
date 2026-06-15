package com.furnisight.order.application.order.service;

import com.furnisight.order.application.order.port.in.command.UpdateOrderStatusCommand;
import com.furnisight.order.application.order.port.in.usecase.UpdateOrderStatusUseCase;
import com.furnisight.order.application.processing.OrderOperation;
import com.furnisight.order.application.processing.OrderProcessingCommand;
import com.furnisight.order.application.processing.OrderProcessingService;
import com.furnisight.order.domain.entities.order.Order;
import com.furnisight.order.domain.enums.OrderStatus;
import com.furnisight.order.domain.enums.PaymentType;
import com.furnisight.order.domain.exceptions.ErrorCode;
import com.furnisight.order.domain.exceptions.ValidationException;
import com.furnisight.order.domain.repository.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UpdateOrderStatusService implements UpdateOrderStatusUseCase {
    private static final Map<OrderStatus, OrderOperation> STATUS_OPERATIONS = statusOperations();

    private final OrderRepository orderRepository;
    private final OrderProcessingService orderProcessingService;

    @Override
    public void updateOrderStatus(UpdateOrderStatusCommand command) {
        Order order = findOrder(command.getOrderCode());
        OrderStatus targetStatus = OrderStatus.valueOf(command.getStatus().trim().toUpperCase());
        OrderOperation operation = STATUS_OPERATIONS.getOrDefault(
                targetStatus, OrderOperation.TRANSITION_STATUS
        );
        process(order, operation, targetStatus, command.getActorId(), command.getActorType(),
                command.getTrackingCode(), command.getNote());
    }

    @Override
    public void shipOrder(String orderCode) {
        process(findOrder(orderCode), OrderOperation.TRANSITION_STATUS, OrderStatus.SHIPPING,
                null, "ADMIN", null, null);
    }

    @Override
    public void deliverOrder(String orderCode) {
        process(findOrder(orderCode), OrderOperation.TRANSITION_STATUS, OrderStatus.DELIVERED,
                null, "ADMIN", null, null);
    }

    @Override
    public void refundOrder(String orderCode) {
        process(findOrder(orderCode), OrderOperation.CONFIRM_REFUND, OrderStatus.REFUNDED,
                null, "ADMIN", null, null);
    }

    @Override
    public void cancelOrder(String orderCode, java.util.UUID userId) {
        Order order = findOrder(orderCode);

        if (!order.getUserId().equals(userId)) {
            throw new ValidationException(ErrorCode.ORDER_NOT_FOUND);
        }

        process(order, OrderOperation.CANCEL, null, userId, "CUSTOMER", null, null);
    }

    private Order findOrder(String orderCode) {
        return orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new ValidationException(ErrorCode.ORDER_NOT_FOUND));
    }

    private void process(Order order, OrderOperation operation, OrderStatus targetStatus,
                         java.util.UUID actorId, String actorType, String trackingCode, String note) {
        orderProcessingService.process(OrderProcessingCommand.builder()
                .operation(operation)
                .order(order)
                .paymentType(PaymentType.from(order.getPaymentDetail().getPaymentMethod()))
                .targetStatus(targetStatus)
                .actorId(actorId)
                .actorType(actorType)
                .trackingCode(trackingCode)
                .note(note)
                .build());
    }

    private static Map<OrderStatus, OrderOperation> statusOperations() {
        Map<OrderStatus, OrderOperation> operations = new EnumMap<>(OrderStatus.class);
        operations.put(OrderStatus.CANCELLED, OrderOperation.CANCEL);
        operations.put(OrderStatus.REFUNDED, OrderOperation.CONFIRM_REFUND);
        return Map.copyOf(operations);
    }
}
