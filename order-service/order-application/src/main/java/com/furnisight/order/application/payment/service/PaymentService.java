package com.furnisight.order.application.payment.service;

import com.furnisight.order.application.payment.port.in.command.CreatePaymentCommand;
import com.furnisight.order.application.payment.port.in.usecase.CreatePaymentUseCase;
import com.furnisight.order.application.payment.port.in.usecase.ProcessPaymentCallbackUseCase;
import com.furnisight.order.application.processing.OrderOperation;
import com.furnisight.order.application.processing.OrderProcessingCommand;
import com.furnisight.order.application.processing.OrderProcessingService;
import com.furnisight.order.domain.enums.PaymentType;
import com.furnisight.order.domain.entities.order.Order;
import com.furnisight.order.domain.exceptions.ErrorCode;
import com.furnisight.order.domain.exceptions.ValidationException;
import com.furnisight.order.domain.repository.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService implements CreatePaymentUseCase, ProcessPaymentCallbackUseCase {

    private final OrderRepository orderRepository;
    private final OrderProcessingService orderProcessingService;

    @Override
    public String createPaymentUrl(CreatePaymentCommand command) {
        Order order = orderRepository.findByOrderCode(command.getOrderCode())
                .orElseThrow(() -> new ValidationException(ErrorCode.ORDER_NOT_FOUND));

        var result = orderProcessingService.process(OrderProcessingCommand.builder()
                .operation(OrderOperation.INITIATE_PAYMENT)
                .order(order)
                .paymentType(PaymentType.from(command.getPaymentMethod()))
                .clientIp(command.getClientIp())
                .actorId(order.getUserId())
                .actorType("CUSTOMER")
                .build());
        if (!result.successful() || result.paymentUrl() == null) {
            throw new ValidationException(ErrorCode.INVALID_ORDER_STATUS);
        }
        return result.paymentUrl();
    }

    @Override
    public boolean processCallback(String paymentMethod, Map<String, String> callbackParams) {
        return orderProcessingService.process(OrderProcessingCommand.builder()
                .operation(OrderOperation.PROCESS_CALLBACK)
                .paymentType(PaymentType.from(paymentMethod))
                .callbackParams(callbackParams)
                .actorType("PAYMENT_GATEWAY")
                .build()).successful();
    }
}
