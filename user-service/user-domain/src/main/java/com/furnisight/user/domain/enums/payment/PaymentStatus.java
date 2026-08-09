package com.furnisight.user.domain.enums.payment;

import java.util.List;

public enum PaymentStatus {
    PENDING,
    AUTHORIZED,
    COMPLETED,
    FAILED,
    EXPIRED,
    CANCELLED;


    public boolean canTransitionTo(PaymentStatus nextStatus) {
        return switch (this) {
            case PENDING -> List.of(COMPLETED, FAILED, EXPIRED, CANCELLED).contains(nextStatus);
            case AUTHORIZED -> List.of(COMPLETED, FAILED, CANCELLED).contains(nextStatus);
            case COMPLETED -> false;
            case FAILED, EXPIRED, CANCELLED -> false;
        };
    }
}
