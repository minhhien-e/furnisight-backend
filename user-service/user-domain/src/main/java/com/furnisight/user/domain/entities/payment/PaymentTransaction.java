package com.furnisight.user.domain.entities.payment;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PaymentTransaction {
    private final UUID id;
    private final UUID sessionId;
    private String gateway;
    private String gatewayTxnId;
    private String type;
    @Getter
    private BigDecimal amount;
    @Setter
    private String status;
    private LocalDateTime createdAt;

    public PaymentTransaction(UUID id, UUID sessionId, BigDecimal amount, String type) {
        this.id = id;
        this.sessionId = sessionId;
        this.amount = amount;
        this.type = type;
        this.createdAt = LocalDateTime.now();
    }


    public boolean isCharge() {
        return "CHARGE".equalsIgnoreCase(this.type);
    }


    public boolean isSuccess() {
        return "SUCCESS".equalsIgnoreCase(this.status);
    }

}
