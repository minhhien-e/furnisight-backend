package com.furnisight.order.application.status.handler;

import com.furnisight.order.application.processing.OrderProcessingContext;
import com.furnisight.order.application.workflow.OrderStatusTransitionValidator;
import com.furnisight.order.domain.enums.OrderStatus;

import java.util.Set;

public abstract class AbstractOrderStatusHandler implements OrderStatusHandler {
    private final OrderStatusTransitionValidator validator;

    protected AbstractOrderStatusHandler(OrderStatusTransitionValidator validator) {
        this.validator = validator;
    }

    protected abstract Set<OrderStatus> allowedTargets(OrderProcessingContext context);

    @Override
    public void handle(OrderProcessingContext context) {
        validator.validate(context.getTargetStatus(), allowedTargets(context));
        context.getOrder().transitionTo(context.getTargetStatus());
    }
}
