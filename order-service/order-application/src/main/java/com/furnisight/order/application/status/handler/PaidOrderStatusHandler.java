package com.furnisight.order.application.status.handler;

import com.furnisight.order.application.processing.OrderProcessingContext;
import com.furnisight.order.application.workflow.OrderStatusTransitionValidator;
import com.furnisight.order.domain.enums.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class PaidOrderStatusHandler extends AbstractOrderStatusHandler {
    public PaidOrderStatusHandler(OrderStatusTransitionValidator validator) { super(validator); }
    public OrderStatus status() { return OrderStatus.PAID; }
    protected Set<OrderStatus> allowedTargets(OrderProcessingContext context) {
        return context.getOrder().isCodOrder()
                ? Set.of(OrderStatus.SHIPPING, OrderStatus.DELIVERED, OrderStatus.CANCELLED)
                : Set.of(OrderStatus.SHIPPING, OrderStatus.REFUND_PENDING);
    }
}
