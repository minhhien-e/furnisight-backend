package com.furnisight.order.domain.events;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderPaidEvent implements DomainEvent {
    private final String orderCode;
    private final Double paidAmount;
    private final String paymentMethod;
}
