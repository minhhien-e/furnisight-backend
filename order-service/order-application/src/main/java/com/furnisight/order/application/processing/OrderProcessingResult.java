package com.furnisight.order.application.processing;

import com.furnisight.order.domain.entities.order.Order;

public record OrderProcessingResult(Order order, boolean successful, String paymentUrl) {
}
