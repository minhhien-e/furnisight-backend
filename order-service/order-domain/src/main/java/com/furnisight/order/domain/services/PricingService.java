package com.furnisight.order.domain.services;

import com.furnisight.order.domain.entities.order.Order;
import com.furnisight.order.domain.services.dto.OrderItemParam;
import com.furnisight.order.domain.valueobjects.OrderFee;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PricingService {

    public double calculateSubTotal(List<OrderItemParam> items) {
        if (items == null || items.isEmpty()) {
            return 0.0;
        }
        return items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }

    public void calculateOrderTotals(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            order.setSubTotal(0.0);
            order.setTotalAmount(0.0);
            order.setSavedAmount(0.0);
            return;
        }

        // Calculate Subtotal
        double subTotal = order.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        order.setSubTotal(subTotal);

        OrderFee fee = order.getFee();
        // Safe check for fees
        double shipping = fee != null && fee.getShippingFee() != null ? fee.getShippingFee() : 0.0;
        double shippingDisc = fee != null && fee.getShippingDiscount() != null ? fee.getShippingDiscount() : 0.0;
        double discount = fee != null && fee.getDiscountAmount() != null ? fee.getDiscountAmount() : 0.0;
        double insurance = fee != null && fee.getInsuranceFee() != null ? fee.getInsuranceFee() : 0.0;

        // Calculate Total
        double totalAmount = subTotal + shipping + insurance - shippingDisc - discount;
        if (totalAmount < 0) {
            totalAmount = 0.0;
        }
        order.setTotalAmount(totalAmount);

        order.setSavedAmount(shippingDisc + discount);
    }
}
