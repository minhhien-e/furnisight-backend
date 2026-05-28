package com.furnisight.order.domain.valueobjects;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentTimeline {
    private LocalDateTime orderCreatedAt;
    private LocalDateTime paymentInitiatedAt;
    private LocalDateTime paymentCompletedAt;
    private LocalDateTime paymentFailedAt;

    @Builder
    public PaymentTimeline(LocalDateTime orderCreatedAt, LocalDateTime paymentInitiatedAt, LocalDateTime paymentCompletedAt, LocalDateTime paymentFailedAt) {
        this.orderCreatedAt = orderCreatedAt;
        this.paymentInitiatedAt = paymentInitiatedAt;
        this.paymentCompletedAt = paymentCompletedAt;
        this.paymentFailedAt = paymentFailedAt;
    }

    public PaymentTimeline initiatePayment(LocalDateTime initiatedAt) {
        return PaymentTimeline.builder()
                .orderCreatedAt(this.orderCreatedAt)
                .paymentInitiatedAt(initiatedAt)
                .paymentCompletedAt(this.paymentCompletedAt)
                .paymentFailedAt(this.paymentFailedAt)
                .build();
    }

    public PaymentTimeline completePayment(LocalDateTime completedAt) {
        return PaymentTimeline.builder()
                .orderCreatedAt(this.orderCreatedAt)
                .paymentInitiatedAt(this.paymentInitiatedAt)
                .paymentCompletedAt(completedAt)
                .paymentFailedAt(this.paymentFailedAt)
                .build();
    }

    public PaymentTimeline failPayment(LocalDateTime failedAt) {
        return PaymentTimeline.builder()
                .orderCreatedAt(this.orderCreatedAt)
                .paymentInitiatedAt(this.paymentInitiatedAt)
                .paymentCompletedAt(this.paymentCompletedAt)
                .paymentFailedAt(failedAt)
                .build();
    }
}
