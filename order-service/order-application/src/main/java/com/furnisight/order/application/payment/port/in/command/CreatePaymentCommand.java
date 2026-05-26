package com.furnisight.order.application.payment.port.in.command;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreatePaymentCommand {
    private String orderCode;
    private String clientIp;
    private String paymentMethod;
}
