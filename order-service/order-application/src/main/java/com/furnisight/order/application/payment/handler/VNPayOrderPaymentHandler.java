package com.furnisight.order.application.payment.handler;

import com.furnisight.order.application.order.service.ExpireUnpaidOrdersService;
import com.furnisight.order.application.payment.port.out.PaymentCallbackResult;
import com.furnisight.order.application.payment.port.out.PaymentGatewayPort;
import com.furnisight.order.application.processing.OrderOperation;
import com.furnisight.order.application.processing.OrderProcessingContext;
import com.furnisight.order.domain.enums.OrderStatus;
import com.furnisight.order.domain.enums.PaymentType;
import com.furnisight.order.domain.exceptions.ErrorCode;
import com.furnisight.order.domain.exceptions.ValidationException;
import com.furnisight.order.domain.repository.order.OrderRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@Component
public class VNPayOrderPaymentHandler implements OrderPaymentHandler {
    private final PaymentGatewayPort gateway;
    private final OrderRepository orderRepository;
    private final Map<OrderOperation, Consumer<OrderProcessingContext>> operations =
            new EnumMap<>(OrderOperation.class);

    public VNPayOrderPaymentHandler(
            PaymentGatewayRegistry gateways,
            OrderRepository orderRepository
    ) {
        this.gateway = gateways.resolve(PaymentType.VNPAY);
        this.orderRepository = orderRepository;
        operations.put(OrderOperation.CREATE, context -> { });
        operations.put(OrderOperation.INITIATE_PAYMENT, this::initiate);
        operations.put(OrderOperation.PROCESS_CALLBACK, this::callback);
        operations.put(OrderOperation.CANCEL, this::prepareCancellation);
        operations.put(OrderOperation.CONFIRM_REFUND, context -> context.setTargetStatus(OrderStatus.REFUNDED));
        operations.put(OrderOperation.TRANSITION_STATUS, context -> { });
    }

    @Override
    public PaymentType paymentType() {
        return PaymentType.VNPAY;
    }

    @Override
    public void process(OrderProcessingContext context) {
        Consumer<OrderProcessingContext> operation = operations.get(context.getOperation());
        if (operation == null) {
            throw new ValidationException(ErrorCode.INVALID_PAYMENT_METHOD);
        }
        operation.accept(context);
    }

    private void initiate(OrderProcessingContext context) {
        LocalDateTime now = LocalDateTime.now();
        if (!ExpireUnpaidOrdersService.isPaymentWindowOpen(context.getOrder(), now)) {
            context.setTargetStatus(OrderStatus.CANCELLED);
            context.setSuccessful(false);
            return;
        }
        context.getOrder().recordPaymentInitiated(now);
        context.setPaymentUrl(gateway.generatePaymentUrl(context.getOrder(), context.getClientIp()));
    }

    private void callback(OrderProcessingContext context) {
        PaymentCallbackResult result = gateway.processCallback(context.getCallbackParams());
        context.setOrder(orderRepository.findByOrderCode(result.getOrderCode())
                .orElseThrow(() -> new ValidationException(ErrorCode.ORDER_NOT_FOUND)));

        OrderStatus currentStatus = context.getOrder().getStatus();
        context.setPreviousStatus(currentStatus);
        if (currentStatus == OrderStatus.PAID) {
            return;
        }
        if (Set.of(OrderStatus.CANCELLED, OrderStatus.REFUND_PENDING, OrderStatus.REFUNDED)
                .contains(currentStatus)) {
            context.setSuccessful(false);
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        if (!ExpireUnpaidOrdersService.isPaymentWindowOpen(context.getOrder(), now)) {
            context.setTargetStatus(OrderStatus.CANCELLED);
            context.setSuccessful(false);
            return;
        }
        if (currentStatus == OrderStatus.PAYMENT_FAILED && !result.isSuccess()) {
            context.setSuccessful(false);
            return;
        }
        if (!result.isSuccess()) {
            context.getOrder().recordPaymentFailure("VNPAY", now);
            context.setTargetStatus(OrderStatus.PAYMENT_FAILED);
            context.setSuccessful(false);
            return;
        }
        context.getOrder().recordPaymentSuccess("VNPAY", result.getAmount(), result.getPaidAt());
        context.setTargetStatus(OrderStatus.PAID);
    }

    private void prepareCancellation(OrderProcessingContext context) {
        context.setTargetStatus(Set.of(OrderStatus.PAID, OrderStatus.IN_TRANSIT).contains(context.getOrder().getStatus())
                ? OrderStatus.REFUND_PENDING
                : OrderStatus.CANCELLED);
    }
}
