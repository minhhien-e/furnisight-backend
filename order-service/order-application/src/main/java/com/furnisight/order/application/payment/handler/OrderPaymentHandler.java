package com.furnisight.order.application.payment.handler;

import com.furnisight.order.application.processing.OrderProcessingContext;
import com.furnisight.order.domain.enums.PaymentType;

public interface OrderPaymentHandler {
    PaymentType paymentType();
    void process(OrderProcessingContext context);
}
