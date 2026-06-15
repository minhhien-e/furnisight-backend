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
    void unpaidCodOrderCanMoveToShipping() {
        Order order = order(OrderStatus.UNPAID, "cod");

        order.shipOrder();

        assertEquals(OrderStatus.SHIPPING, order.getStatus());
    }

    @Test
    void paidCodOrderCanMoveToShipping() {
        Order order = order(OrderStatus.PAID, "cod");

        order.shipOrder();

        assertEquals(OrderStatus.SHIPPING, order.getStatus());
    }

    @Test
    void cancellingPaidCodOrderDoesNotRequireRefund() {
        Order order = order(OrderStatus.PAID, "cod");

        order.cancelOrder();

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void paidCodOrderCanBeCompletedDirectly() {
        Order order = order(OrderStatus.PAID, "cod");

        order.deliverOrder();

        assertEquals(OrderStatus.DELIVERED, order.getStatus());
    }

    @Test
    void legacyUnpaidCodOrderCanBeCompletedDirectly() {
        Order order = order(OrderStatus.UNPAID, "cod");

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
