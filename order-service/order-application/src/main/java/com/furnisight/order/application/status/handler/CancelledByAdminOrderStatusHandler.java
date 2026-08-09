package com.furnisight.order.application.status.handler;

import com.furnisight.order.application.processing.OrderProcessingContext;
import com.furnisight.order.application.workflow.OrderStatusTransitionValidator;
import com.furnisight.order.domain.enums.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class CancelledByAdminOrderStatusHandler extends AbstractOrderStatusHandler {
    public CancelledByAdminOrderStatusHandler(OrderStatusTransitionValidator validator) { super(validator); }
    public OrderStatus status() { return OrderStatus.CANCELLED_BY_ADMIN; }
    protected Set<OrderStatus> allowedTargets(OrderProcessingContext context) { return Set.of(); }
}
