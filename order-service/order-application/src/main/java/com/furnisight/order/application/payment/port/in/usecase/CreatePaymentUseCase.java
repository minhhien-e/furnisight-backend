package com.furnisight.order.application.payment.port.in.usecase;

import com.furnisight.order.application.payment.port.in.command.CreatePaymentCommand;

public interface CreatePaymentUseCase {
    String createPaymentUrl(CreatePaymentCommand command);
}
