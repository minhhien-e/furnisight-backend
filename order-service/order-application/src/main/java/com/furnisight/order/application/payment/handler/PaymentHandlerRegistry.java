package com.furnisight.order.application.payment.handler;

import com.furnisight.order.domain.enums.PaymentType;
import com.furnisight.order.domain.exceptions.ErrorCode;
import com.furnisight.order.domain.exceptions.ValidationException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PaymentHandlerRegistry {
    private final Map<PaymentType, OrderPaymentHandler> handlers = new EnumMap<>(PaymentType.class);

    public PaymentHandlerRegistry(List<OrderPaymentHandler> handlers) {
        handlers.forEach(handler -> {
            if (this.handlers.put(handler.paymentType(), handler) != null) {
                throw new IllegalStateException("Duplicate payment handler: " + handler.paymentType());
            }
        });
        for (PaymentType paymentType : PaymentType.values()) {
            if (!this.handlers.containsKey(paymentType)) {
                throw new IllegalStateException("Missing payment handler: " + paymentType);
            }
        }
    }

    public OrderPaymentHandler resolve(PaymentType paymentType) {
        OrderPaymentHandler handler = handlers.get(paymentType);
        if (handler == null) {
            throw new ValidationException(ErrorCode.INVALID_PAYMENT_METHOD);
        }
        return handler;
    }
}
