package com.furnisight.order.application.payment.handler;

import com.furnisight.order.domain.enums.PaymentType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PaymentHandlerRegistryTest {
    @Test
    void resolvesEveryRegisteredPaymentHandler() {
        OrderPaymentHandler cod = handler(PaymentType.COD);
        OrderPaymentHandler vnpay = handler(PaymentType.VNPAY);

        PaymentHandlerRegistry registry = new PaymentHandlerRegistry(List.of(cod, vnpay));

        assertSame(cod, registry.resolve(PaymentType.COD));
        assertSame(vnpay, registry.resolve(PaymentType.VNPAY));
    }

    @Test
    void failsFastWhenAHandlerIsMissing() {
        assertThrows(IllegalStateException.class,
                () -> new PaymentHandlerRegistry(List.of(handler(PaymentType.COD))));
    }

    @Test
    void failsFastWhenAHandlerIsDuplicated() {
        assertThrows(IllegalStateException.class,
                () -> new PaymentHandlerRegistry(List.of(
                        handler(PaymentType.COD),
                        handler(PaymentType.COD),
                        handler(PaymentType.VNPAY)
                )));
    }

    private OrderPaymentHandler handler(PaymentType paymentType) {
        OrderPaymentHandler handler = mock(OrderPaymentHandler.class);
        when(handler.paymentType()).thenReturn(paymentType);
        return handler;
    }
}
