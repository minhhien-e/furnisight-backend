package com.furnisight.user.domain.entities.payment;

import com.furnisight.user.domain.enums.payment.PaymentStatus;
import com.furnisight.user.domain.exceptions.DomainException;
import com.furnisight.user.domain.exceptions.identity.ErrorCode;
import com.furnisight.user.domain.valueobjects.payment.Money;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PaymentSession {
    private UUID id;
    private Money totalAmount;
    private List<PaymentTransaction> transactions = new ArrayList<>();
    private PaymentStatus status;

    public void addTransaction(PaymentTransaction txn) {

        if (!this.status.canTransitionTo(PaymentStatus.COMPLETED)) {
            throw new DomainException(ErrorCode.INVALID_GATEWAY_RESPONSE);
        }


        if (txn.isCharge() && !txn.getAmount().equals(this.totalAmount)) {
            throw new DomainException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        this.transactions.add(txn);


        if (txn.isSuccess()) {
            this.status = PaymentStatus.COMPLETED;
        }
    }
}
