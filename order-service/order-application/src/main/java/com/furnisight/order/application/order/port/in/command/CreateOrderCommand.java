package com.furnisight.order.application.order.port.in.command;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;
import java.util.List;

@Data
@Builder
public class CreateOrderCommand {
    private UUID userId;
    private String shippingAddressName;
    private String shippingAddressPhone;
    private String shippingAddressDetail;
    private String shippingMethod;
    private String customerNote;
    private String paymentMethod;
    
    private List<OrderItemCommand> items;

    @Data
    @Builder
    public static class OrderItemCommand {
        private String productId;
        private String variantId;
        private String categoryName;
        private String productName;
        private String variantDescription;
        private Double price;
        private Double oldPrice;
        private Integer quantity;
        private String imageUrl;
    }
}
