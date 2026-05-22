package com.furnisight.order.application.order.port.in.dto.projection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailProjection {
    private UUID id;
    private String status;
    private LocalDateTime createdAt;
    
    // Address Info
    private String shippingAddressName;
    private String shippingAddressPhone;
    private String shippingAddressDetail;
    
    // Delivery & Note Info
    private String shippingMethod;
    private String customerNote;
    
    // Payment Info
    private String paymentMethod;
    private String paymentStatus;
    private Double paidAmount;
    private LocalDateTime paidAt;
    
    // Summary Info
    private Double subTotal;
    private Double shippingFee;
    private Double shippingDiscount;
    private Double discountAmount;
    private Double insuranceFee;
    private Double savedAmount;
    private Double totalAmount;
    
    // Products Info
    private List<OrderDetailItemProjection> items;
}
