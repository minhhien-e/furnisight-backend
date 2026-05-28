package com.furnisight.order.domain.valueobjects;

import com.furnisight.order.domain.exceptions.ErrorCode;
import com.furnisight.order.domain.exceptions.ValidationException;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentDetail {
    private String paymentMethod;
    private String paymentStatus;
    private Double paidAmount;
    private LocalDateTime paidAt;

    @Builder
    public PaymentDetail(String paymentMethod, String paymentStatus, Double paidAmount, LocalDateTime paidAt) {
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            throw new ValidationException(ErrorCode.INVALID_PAYMENT_METHOD);
        }
        if (paidAmount != null && paidAmount < 0) {
            throw new ValidationException(ErrorCode.INVALID_PAYMENT_AMOUNT);
        }
        
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.paidAmount = paidAmount != null ? paidAmount : 0.0;
        this.paidAt = paidAt;
    }

    public boolean isPaid() {
        return "PAID".equalsIgnoreCase(paymentStatus) || (paidAmount != null && paidAmount > 0);
    }
}
