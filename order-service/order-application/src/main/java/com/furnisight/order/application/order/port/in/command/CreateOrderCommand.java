package com.furnisight.order.application.order.port.in.command;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;
import java.util.List;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderCommand {
    private UUID userId;
    private String shippingAddressName;
    private String shippingAddressPhone;
    private String shippingAddressDetail;
    private String shippingMethod;
    private String customerNote;
    private String customerEmail;
    private String paymentMethod;
    private String shopVoucherCode;
    private String shippingVoucherCode;
    private String comboId;
    private Double discountAmount;
    private Double shippingDiscount;
    private Double comboDiscount;
    private Double shippingFee;
    private Double insuranceFee;
    
    private List<OrderItemCommand> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemCommand {
        private String productId;
        private String variantId;
        private String categoryName;
        private String productName;
        private Double price;
        private Integer quantity;
        private String imageUrl;
    }
}
