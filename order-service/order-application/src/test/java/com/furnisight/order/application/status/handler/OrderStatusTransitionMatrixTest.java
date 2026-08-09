package com.furnisight.order.application.status.handler;

import com.furnisight.order.application.processing.OrderOperation;
import com.furnisight.order.application.processing.OrderProcessingCommand;
import com.furnisight.order.application.processing.OrderProcessingContext;
import com.furnisight.order.application.workflow.OrderStatusTransitionValidator;
import com.furnisight.order.domain.entities.order.Order;
import com.furnisight.order.domain.enums.OrderStatus;
import com.furnisight.order.domain.enums.PaymentType;
import com.furnisight.order.domain.exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderStatusTransitionMatrixTest {
    private final OrderStatusTransitionValidator validator = new OrderStatusTransitionValidator();

    @Test
    void confirmedCodCanMoveToShipping() {
        Order order = order(true);
        OrderProcessingContext context = context(order, OrderStatus.SHIPPING);

        new ConfirmedOrderStatusHandler(validator).handle(context);

        verify(order).transitionTo(OrderStatus.SHIPPING);
    }

    @Test
    void unpaidCodMovesToShipping() {
        Order order = order(true);
        OrderProcessingContext context = context(order, OrderStatus.SHIPPING);

        new UnpaidOrderStatusHandler(validator).handle(context);

        verify(order).transitionTo(OrderStatus.SHIPPING);
    }

    @Test
    void paidOnlineCancellationMustWaitForRefund() {
        Order order = order(false);

        assertThrows(ValidationException.class,
                () -> new PaidOrderStatusHandler(validator)
                        .handle(context(order, OrderStatus.CANCELLED)));

        new PaidOrderStatusHandler(validator)
                .handle(context(order, OrderStatus.REFUND_PENDING));
        verify(order).transitionTo(OrderStatus.REFUND_PENDING);
    }

    @Test
    void paidCodCanStillMoveToShipping() {
        Order order = order(true);
        OrderProcessingContext context = context(order, OrderStatus.SHIPPING);

        new PaidOrderStatusHandler(validator).handle(context);

        verify(order).transitionTo(OrderStatus.SHIPPING);
    }

    @Test
    void shippingAllowsInTransit() {
        Order order = order(false);
        ShippingOrderStatusHandler handler = new ShippingOrderStatusHandler(validator);

        handler.handle(context(order, OrderStatus.IN_TRANSIT));
        verify(order).transitionTo(OrderStatus.IN_TRANSIT);
    }

    @Test
    void inTransitOnlyAllowsDelivery() {
        Order order = order(false);
        InTransitOrderStatusHandler handler = new InTransitOrderStatusHandler(validator);

        assertThrows(ValidationException.class,
                () -> handler.handle(context(order, OrderStatus.CANCELLED)));
        handler.handle(context(order, OrderStatus.DELIVERED));
        verify(order).transitionTo(OrderStatus.DELIVERED);
    }

    @Test
    void refundPendingOnlyAllowsRefunded() {
        Order order = order(false);
        RefundPendingOrderStatusHandler handler = new RefundPendingOrderStatusHandler(validator);

        assertThrows(ValidationException.class,
                () -> handler.handle(context(order, OrderStatus.DELIVERED)));
        handler.handle(context(order, OrderStatus.REFUNDED));
        verify(order).transitionTo(OrderStatus.REFUNDED);
    }

    @Test
    void terminalStatesRejectAllTransitions() {
        Order order = order(false);

        assertThrows(ValidationException.class,
                () -> new DeliveredOrderStatusHandler(validator)
                        .handle(context(order, OrderStatus.SHIPPING)));
        assertThrows(ValidationException.class,
                () -> new CancelledOrderStatusHandler(validator)
                        .handle(context(order, OrderStatus.PAID)));
        assertThrows(ValidationException.class,
                () -> new RefundedOrderStatusHandler(validator)
                        .handle(context(order, OrderStatus.PAID)));
    }

    private Order order(boolean cod) {
        Order order = mock(Order.class);
        when(order.isCodOrder()).thenReturn(cod);
        return order;
    }

    private OrderProcessingContext context(Order order, OrderStatus target) {
        return new OrderProcessingContext(OrderProcessingCommand.builder()
                .operation(OrderOperation.TRANSITION_STATUS)
                .order(order)
                .paymentType(PaymentType.VNPAY)
                .targetStatus(target)
                .build());
    }
}
