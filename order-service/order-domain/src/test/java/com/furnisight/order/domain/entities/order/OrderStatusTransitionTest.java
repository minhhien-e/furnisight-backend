package com.furnisight.order.domain.entities.order;

import com.furnisight.order.domain.enums.OrderStatus;
import com.furnisight.order.domain.exceptions.ValidationException;
import com.furnisight.order.domain.valueobjects.PaymentDetail;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class OrderStatusTransitionTest {

    @Test
    void paidOrderMovesFromRefundPendingToRefunded() {
        Order order = order(OrderStatus.PAID, "vnpay");

        order.cancelOrder();
        assertEquals(OrderStatus.REFUND_PENDING, order.getStatus());

        order.markAsRefunded();
        assertEquals(OrderStatus.REFUNDED, order.getStatus());
    }

    @Test
    void onlyRefundPendingOrderCanBeMarkedAsRefunded() {
        Order order = order(OrderStatus.PAID, "vnpay");

        assertThrows(ValidationException.class, order::markAsRefunded);
    }

    @Test
    void confirmedCodOrderCanMoveToShipping() {
        Order order = order(OrderStatus.CONFIRMED, "cod");

        order.shipOrder();

        assertEquals(OrderStatus.SHIPPING, order.getStatus());
    }

    @Test
    void unpaidCodOrderCannotMoveToShippingBeforeConfirmation() {
        Order order = order(OrderStatus.UNPAID, "cod");

        assertThrows(ValidationException.class, order::shipOrder);
    }

    @Test
    void cancellingConfirmedCodOrderDoesNotRequireRefund() {
        Order order = order(OrderStatus.CONFIRMED, "cod");

        order.cancelOrder();

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void confirmedCodOrderCannotBeCompletedBeforeShipping() {
        Order order = order(OrderStatus.CONFIRMED, "cod");

        assertThrows(ValidationException.class, order::deliverOrder);
    }

    @Test
    void shippingCodOrderCanBeCompleted() {
        Order order = order(OrderStatus.SHIPPING, "cod");

        order.deliverOrder();

        assertEquals(OrderStatus.DELIVERED, order.getStatus());
    }

    @Test
    void paidOnlineOrderCannotBeCompletedBeforeShipping() {
        Order order = order(OrderStatus.PAID, "vnpay");

        assertThrows(ValidationException.class, order::deliverOrder);
    }

    private Order order(OrderStatus status, String paymentMethod) {
        return Order.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .orderCode("ORD-TEST")
                .status(status)
                .paymentDetail(PaymentDetail.builder()
                        .paymentMethod(paymentMethod)
                        .paymentStatus(status == OrderStatus.PAID ? "PAID" : "UNPAID")
                        .paidAmount(status == OrderStatus.PAID ? 100_000D : 0D)
                        .build())
                .items(List.of(mock(OrderItem.class)))
                .build();
    }
}
