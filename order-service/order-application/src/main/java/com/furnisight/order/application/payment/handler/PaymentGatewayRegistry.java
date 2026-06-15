package com.furnisight.order.application.payment.handler;

import com.furnisight.order.application.payment.port.out.PaymentGatewayPort;
import com.furnisight.order.domain.enums.PaymentType;
import com.furnisight.order.domain.exceptions.ErrorCode;
import com.furnisight.order.domain.exceptions.ValidationException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PaymentGatewayRegistry {
    private final Map<PaymentType, PaymentGatewayPort> gateways = new EnumMap<>(PaymentType.class);

    public PaymentGatewayRegistry(List<PaymentGatewayPort> gateways) {
        gateways.forEach(gateway -> {
            PaymentType type = PaymentType.from(gateway.getPaymentMethod());
            if (this.gateways.put(type, gateway) != null) {
                throw new IllegalStateException("Duplicate payment gateway: " + type);
            }
        });
    }

    public PaymentGatewayPort resolve(PaymentType type) {
        PaymentGatewayPort gateway = gateways.get(type);
        if (gateway == null) {
            throw new ValidationException(ErrorCode.INVALID_PAYMENT_METHOD);
        }
        return gateway;
    }
}
