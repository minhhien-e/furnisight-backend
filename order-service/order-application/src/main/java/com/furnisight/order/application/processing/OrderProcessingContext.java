package com.furnisight.order.application.processing;

import com.furnisight.order.domain.entities.order.Order;
import com.furnisight.order.domain.enums.OrderStatus;
import com.furnisight.order.domain.enums.PaymentType;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
public class OrderProcessingContext {
    private final OrderOperation operation;
    @Setter
    private Order order;
    private final PaymentType paymentType;
    private final UUID actorId;
    private final String actorType;
    private final String trackingCode;
    private final String note;
    private final String clientIp;
    private final Map<String, String> callbackParams;
    @Setter
    private OrderStatus targetStatus;
    @Setter
    private OrderStatus previousStatus;
    @Setter
    private String paymentUrl;
    @Setter
    private boolean successful = true;

    public OrderProcessingContext(OrderProcessingCommand command) {
        this.operation = command.getOperation();
        this.order = command.getOrder();
        this.paymentType = command.getPaymentType();
        this.targetStatus = command.getTargetStatus();
        this.previousStatus = command.getOrder() == null ? null : command.getOrder().getStatus();
        this.actorId = command.getActorId();
        this.actorType = command.getActorType();
        this.trackingCode = command.getTrackingCode();
        this.note = command.getNote();
        this.clientIp = command.getClientIp();
        this.callbackParams = command.getCallbackParams() == null ? Map.of() : command.getCallbackParams();
    }
}
