package com.furnisight.order.application.workflow;

import com.furnisight.order.domain.enums.OrderStatus;
import com.furnisight.order.domain.exceptions.ErrorCode;
import com.furnisight.order.domain.exceptions.ValidationException;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class OrderStatusTransitionValidator {
    public void validate(OrderStatus target, Set<OrderStatus> allowedTargets) {
        if (target == null || !allowedTargets.contains(target)) {
            throw new ValidationException(ErrorCode.INVALID_ORDER_STATUS);
        }
    }
}
