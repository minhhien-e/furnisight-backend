package com.furnisight.order.application.order.port.in.dto;

import com.furnisight.order.domain.entities.order.Order;
import com.furnisight.order.domain.entities.order.OrderItem;
import com.furnisight.order.domain.entities.order.OrderStatusHistory;
import com.furnisight.order.domain.valueobjects.OrderFee;
import com.furnisight.order.domain.valueobjects.PaymentDetail;
import com.furnisight.order.domain.valueobjects.PaymentTimeline;
import com.furnisight.order.domain.valueobjects.ProductDimensions;
import com.furnisight.order.domain.valueobjects.ProductSnapshot;
import com.furnisight.order.domain.valueobjects.ShippingDetail;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class OrderResponse {
    private UUID id;
    private UUID userId;
    private String orderCode;
    private String status;
    private Double subTotal;
    private Double totalAmount;
    private Double savedAmount;
    private String customerNote;
    private String trackingCode;
    private OrderFeeResponse fee;
    private ShippingDetailResponse shippingDetail;
    private PaymentDetailResponse paymentDetail;
    private PaymentTimelineResponse paymentTimeline;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderStatusHistoryResponse> statusHistory;

    public static OrderResponse summary(Order order) {
        return base(order)
                .items(order.getItems() == null ? null : order.getItems().stream().map(OrderItemResponse::from).toList())
                .build();
    }

    public static OrderResponse created(Order order) {
        return base(order)
                .items(order.getItems() == null ? null : order.getItems().stream().map(OrderItemResponse::from).toList())
                .build();
    }

    public static OrderResponse detail(Order order, List<OrderStatusHistory> statusHistory) {
        return base(order)
                .subTotal(order.getSubTotal())
                .savedAmount(order.getSavedAmount())
                .customerNote(order.getCustomerNote())
                .trackingCode(order.getTrackingCode())
                .fee(OrderFeeResponse.from(order.getFee()))
                .shippingDetail(ShippingDetailResponse.from(order.getShippingDetail()))
                .paymentDetail(PaymentDetailResponse.from(order.getPaymentDetail()))
                .paymentTimeline(PaymentTimelineResponse.from(order.getPaymentTimeline()))
                .items(order.getItems() == null ? null : order.getItems().stream().map(OrderItemResponse::from).toList())
                .updatedAt(order.getUpdatedAt())
                .statusHistory(statusHistory == null ? null : statusHistory.stream().map(OrderStatusHistoryResponse::from).toList())
                .build();
    }

    private static OrderResponseBuilder base(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .orderCode(order.getOrderCode())
                .status(order.getStatus() == null ? null : order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .paymentDetail(PaymentDetailResponse.from(order.getPaymentDetail()));
    }

    @Data
    @Builder
    public static class OrderItemResponse {
        private UUID id;
        private ProductSnapshotResponse productSnapshot;
        private Double price;
        private Integer quantity;

        private static OrderItemResponse from(OrderItem item) {
            if (item == null) {
                return null;
            }
            return OrderItemResponse.builder()
                    .id(item.getId())
                    .productSnapshot(ProductSnapshotResponse.from(item.getProductSnapshot()))
                    .price(item.getPrice())
                    .quantity(item.getQuantity())
                    .build();
        }
    }

    @Data
    @Builder
    public static class ProductSnapshotResponse {
        private String productId;
        private String variantId;
        private String slug;
        private String categoryName;
        private String productName;
        private String color;
        private String material;
        private String warranty;
        private ProductDimensionsResponse dimensions;
        private String imageUrl;

        private static ProductSnapshotResponse from(ProductSnapshot snapshot) {
            if (snapshot == null) {
                return null;
            }
            return ProductSnapshotResponse.builder()
                    .productId(snapshot.getProductId())
                    .variantId(snapshot.getVariantId())
                    .slug(snapshot.getSlug())
                    .categoryName(snapshot.getCategoryName())
                    .productName(snapshot.getProductName())
                    .color(snapshot.getColor())
                    .material(snapshot.getMaterial())
                    .warranty(snapshot.getWarranty())
                    .dimensions(ProductDimensionsResponse.from(snapshot.getDimensions()))
                    .imageUrl(snapshot.getImageUrl())
                    .build();
        }
    }

    @Data
    @Builder
    public static class ProductDimensionsResponse {
        private Double weight;
        private Double length;
        private Double width;
        private Double height;

        private static ProductDimensionsResponse from(ProductDimensions dimensions) {
            if (dimensions == null) {
                return null;
            }
            return ProductDimensionsResponse.builder()
                    .weight(dimensions.getWeight())
                    .length(dimensions.getLength())
                    .width(dimensions.getWidth())
                    .height(dimensions.getHeight())
                    .build();
        }
    }

    @Data
    @Builder
    public static class OrderFeeResponse {
        private Double shippingFee;
        private Double shippingDiscount;
        private Double discountAmount;
        private Double comboDiscount;
        private Double insuranceFee;
        private String shopVoucherCode;
        private String shippingVoucherCode;
        private String comboId;

        private static OrderFeeResponse from(OrderFee fee) {
            if (fee == null) {
                return null;
            }
            return OrderFeeResponse.builder()
                    .shippingFee(fee.getShippingFee())
                    .shippingDiscount(fee.getShippingDiscount())
                    .discountAmount(fee.getDiscountAmount())
                    .comboDiscount(fee.getComboDiscount())
                    .insuranceFee(fee.getInsuranceFee())
                    .shopVoucherCode(fee.getShopVoucherCode())
                    .shippingVoucherCode(fee.getShippingVoucherCode())
                    .comboId(fee.getComboId())
                    .build();
        }
    }

    @Data
    @Builder
    public static class ShippingDetailResponse {
        private String shippingAddressName;
        private String shippingAddressPhone;
        private String shippingAddressDetail;
        private String shippingMethod;

        private static ShippingDetailResponse from(ShippingDetail shippingDetail) {
            if (shippingDetail == null) {
                return null;
            }
            return ShippingDetailResponse.builder()
                    .shippingAddressName(shippingDetail.getShippingAddressName())
                    .shippingAddressPhone(shippingDetail.getShippingAddressPhone())
                    .shippingAddressDetail(shippingDetail.getShippingAddressDetail())
                    .shippingMethod(shippingDetail.getShippingMethod())
                    .build();
        }
    }

    @Data
    @Builder
    public static class PaymentDetailResponse {
        private String paymentMethod;
        private String paymentStatus;
        private Double paidAmount;
        private LocalDateTime paidAt;

        private static PaymentDetailResponse from(PaymentDetail paymentDetail) {
            if (paymentDetail == null) {
                return null;
            }
            return PaymentDetailResponse.builder()
                    .paymentMethod(paymentDetail.getPaymentMethod())
                    .paymentStatus(paymentDetail.getPaymentStatus())
                    .paidAmount(paymentDetail.getPaidAmount())
                    .paidAt(paymentDetail.getPaidAt())
                    .build();
        }
    }

    @Data
    @Builder
    public static class PaymentTimelineResponse {
        private LocalDateTime orderCreatedAt;
        private LocalDateTime paymentInitiatedAt;
        private LocalDateTime paymentCompletedAt;
        private LocalDateTime paymentFailedAt;

        private static PaymentTimelineResponse from(PaymentTimeline timeline) {
            if (timeline == null) {
                return null;
            }
            return PaymentTimelineResponse.builder()
                    .orderCreatedAt(timeline.getOrderCreatedAt())
                    .paymentInitiatedAt(timeline.getPaymentInitiatedAt())
                    .paymentCompletedAt(timeline.getPaymentCompletedAt())
                    .paymentFailedAt(timeline.getPaymentFailedAt())
                    .build();
        }
    }

    @Data
    @Builder
    public static class OrderStatusHistoryResponse {
        private String previousStatus;
        private String nextStatus;
        private String actorType;
        private String trackingCode;
        private String note;
        private LocalDateTime createdAt;

        private static OrderStatusHistoryResponse from(OrderStatusHistory history) {
            if (history == null) {
                return null;
            }
            return OrderStatusHistoryResponse.builder()
                    .previousStatus(history.getPreviousStatus() == null ? null : history.getPreviousStatus().name())
                    .nextStatus(history.getNextStatus() == null ? null : history.getNextStatus().name())
                    .actorType(history.getActorType())
                    .trackingCode(history.getTrackingCode())
                    .note(history.getNote())
                    .createdAt(history.getCreatedAt())
                    .build();
        }
    }
}
