package com.furnisight.order.application.payment.port.out;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentCallbackResult {
    private boolean success;
    private String orderCode;
    private Double amount;
    private LocalDateTime paidAt;
    private String errorMessage;
}
