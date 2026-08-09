package com.furnisight.order.application.processing;

import com.furnisight.order.domain.entities.order.Order;
import com.furnisight.order.domain.enums.OrderStatus;
import com.furnisight.order.domain.enums.PaymentType;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.UUID;

@Getter
@Builder
public class OrderProcessingCommand {
    private OrderOperation operation;
    private Order order;
    private PaymentType paymentType;
    private OrderStatus targetStatus;
    private UUID actorId;
    private String actorType;
    private String trackingCode;
    private String note;
    private String clientIp;
    private Map<String, String> callbackParams;
}
