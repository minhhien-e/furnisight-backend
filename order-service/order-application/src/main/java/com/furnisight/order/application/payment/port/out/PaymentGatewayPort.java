package com.furnisight.order.application.payment.port.out;

import com.furnisight.order.domain.entities.order.Order;

public interface PaymentGatewayPort {
    String getPaymentMethod();
    String generatePaymentUrl(Order order, String clientIp);
    PaymentCallbackResult processCallback(java.util.Map<String, String> callbackParams);
}
