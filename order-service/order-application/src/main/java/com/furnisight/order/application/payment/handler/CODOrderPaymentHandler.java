package com.furnisight.order.application.payment.handler;

import com.furnisight.order.application.processing.OrderOperation;
import com.furnisight.order.application.processing.OrderProcessingContext;
import com.furnisight.order.domain.enums.OrderStatus;
import com.furnisight.order.domain.enums.PaymentType;
import com.furnisight.order.domain.exceptions.ErrorCode;
import com.furnisight.order.domain.exceptions.ValidationException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

@Component
public class CODOrderPaymentHandler implements OrderPaymentHandler {
    private final Map<OrderOperation, Consumer<OrderProcessingContext>> operations =
            new EnumMap<>(OrderOperation.class);

    public CODOrderPaymentHandler() {
        operations.put(OrderOperation.CREATE, context -> { });
        operations.put(OrderOperation.CANCEL, this::prepareCancellation);
        operations.put(OrderOperation.TRANSITION_STATUS, context -> { });
    }

    @Override
    public PaymentType paymentType() {
        return PaymentType.COD;
    }

    @Override
    public void process(OrderProcessingContext context) {
        Consumer<OrderProcessingContext> operation = operations.get(context.getOperation());
        if (operation == null) {
            throw new ValidationException(ErrorCode.INVALID_PAYMENT_METHOD);
        }
        operation.accept(context);
    }

    private void prepareCancellation(OrderProcessingContext context) {
        context.setTargetStatus(OrderStatus.CANCELLED);
    }
}
