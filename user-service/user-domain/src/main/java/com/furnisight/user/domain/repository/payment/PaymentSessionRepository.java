package com.furnisight.user.domain.repository.payment;


import com.furnisight.user.domain.entities.payment.PaymentSession;

import java.util.Optional;
import java.util.UUID;

public interface PaymentSessionRepository {
    Optional<PaymentSession> findById(UUID id);
    Optional<PaymentSession> findByOrderId(String orderId);
    void save(PaymentSession paymentSession);
}
