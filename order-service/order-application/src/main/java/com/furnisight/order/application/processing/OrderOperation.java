package com.furnisight.order.application.processing;

public enum OrderOperation {
    CREATE,
    INITIATE_PAYMENT,
    PROCESS_CALLBACK,
    TRANSITION_STATUS,
    CANCEL,
    CONFIRM_REFUND
}
