package com.furnisight.order.application.payment.port.in.usecase;

import java.util.Map;

public interface ProcessPaymentCallbackUseCase {
    boolean processCallback(String paymentMethod, Map<String, String> callbackParams);
}
