package com.furnisight.order.adapter.in.web.rest;

import com.furnisight.order.application.order.port.in.command.CreateOrderCommand;
import com.furnisight.order.application.order.service.ExpireUnpaidOrdersService;
import com.furnisight.order.application.order.port.in.usecase.CreateOrderUseCase;
import com.furnisight.order.application.order.port.in.usecase.GetOrderQuery;
import com.furnisight.order.application.order.port.in.usecase.UpdateOrderStatusUseCase;
import com.furnisight.order.domain.entities.order.Order;
import com.furnisight.order.domain.entities.order.OrderItem;
import com.furnisight.order.domain.enums.OrderStatus;
import com.furnisight.order.application.order.port.in.dto.OrderCreateProjection;
import com.furnisight.order.adapter.in.web.dto.response.OrderListResponse;
import com.furnisight.order.adapter.in.web.dto.response.OrderDetailResponse;
import com.furnisight.order.adapter.in.web.dto.response.OrderStatusHistoryResponse;
import com.furnisight.order.adapter.in.web.dto.response.OrderItemResponse;
import com.furnisight.order.adapter.in.web.dto.response.PaymentDetailResponse;
import com.furnisight.order.adapter.in.web.dto.response.PaymentTimelineResponse;
import com.furnisight.order.adapter.in.web.dto.response.ProductPurchaseCheckResponse;
import com.furnisight.order.domain.valueobjects.ProductSnapshot;
import com.furnisight.order.domain.valueobjects.PaymentDetail;
import com.furnisight.order.domain.valueobjects.PaymentTimeline;
import com.furnisight.order.domain.repository.order.OrderStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderQuery getOrderQuery;
    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;
    private final com.furnisight.order.application.common.port.in.CurrentUserProvider currentUserProvider;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    @PostMapping("/initiate")
    @PreAuthorize("isAuthenticated() and !hasRole('ADMIN')")
    public ResponseEntity<OrderCreateProjection> initiateOrder(@RequestBody CreateOrderCommand command) {
        command.setUserId(currentUserProvider.getCurrentUserId());
        OrderCreateProjection projection = createOrderUseCase.createOrder(command);
        return ResponseEntity.ok(projection);
    }

    @GetMapping("/user")
    public ResponseEntity<List<OrderListResponse>> getUserOrders() {
        UUID userId = currentUserProvider.getCurrentUserId();
        List<Order> orders = getOrderQuery.getUserOrders(userId);

        List<OrderListResponse> response = orders.stream().map(order -> {
            String firstImage = resolveFirstProductImage(order);
            return OrderListResponse.builder()
                    .id(order.getId())
                    .orderCode(order.getOrderCode())
                    .status(order.getStatus().name())
                    .statusLabel(toStatusLabel(order))
                    .totalAmount(order.getTotalAmount())
                    .createdAt(order.getCreatedAt())
                    .paymentExpiresAt(ExpireUnpaidOrdersService.paymentExpiresAt(order))
                    .paymentMethod(resolvePaymentMethod(order))
                    .canRetryPayment(canRetryPayment(order))
                    .firstProductImage(firstImage)
                    .build();
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/products/{productId}/purchased")
    public ResponseEntity<ProductPurchaseCheckResponse> checkPurchasedProduct(@PathVariable String productId) {
        UUID userId = currentUserProvider.getCurrentUserId();
        UUID orderItemId = getOrderQuery.getDeliveredOrderItemIdForProduct(userId, productId).orElse(null);

        return ResponseEntity.ok(ProductPurchaseCheckResponse.builder()
                .purchased(orderItemId != null)
                .orderItemId(orderItemId != null ? orderItemId.toString() : null)
                .build());
    }

    @GetMapping("/{orderCode}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(@PathVariable String orderCode) {
        Order order = getOrderQuery.getOrderDetail(orderCode);

        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .productSnapshot(item.getProductSnapshot())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        OrderDetailResponse response = OrderDetailResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .status(order.getStatus().name())
                .statusLabel(toStatusLabel(order))
                .subTotal(order.getSubTotal())
                .totalAmount(order.getTotalAmount())
                .savedAmount(order.getSavedAmount())
                .customerNote(order.getCustomerNote())
                .trackingCode(order.getTrackingCode())
                .fee(order.getFee())
                .shippingDetail(order.getShippingDetail())
                .paymentDetail(toPaymentDetailResponse(order))
                .paymentTimeline(toPaymentTimelineResponse(order))
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .paymentExpiresAt(ExpireUnpaidOrdersService.paymentExpiresAt(order))
                .canRetryPayment(canRetryPayment(order))
                .statusHistory(orderStatusHistoryRepository.findByOrderId(order.getId()).stream()
                        .map(history -> OrderStatusHistoryResponse.builder()
                                .previousStatus(history.getPreviousStatus().name())
                                .nextStatus(history.getNextStatus().name())
                                .actorType(history.getActorType())
                                .trackingCode(history.getTrackingCode())
                                .note(history.getNote())
                                .createdAt(history.getCreatedAt())
                                .build())
                        .toList())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin")
    public ResponseEntity<List<OrderListResponse>> getAdminOrders(@RequestParam(required = false) String status) {
        List<Order> orders = getOrderQuery.getAdminOrders(status);

        List<OrderListResponse> response = orders.stream().map(order -> {
            String firstImage = resolveFirstProductImage(order);
            return OrderListResponse.builder()
                    .id(order.getId())
                    .orderCode(order.getOrderCode())
                    .status(order.getStatus().name())
                    .statusLabel(toStatusLabel(order))
                    .totalAmount(order.getTotalAmount())
                    .createdAt(order.getCreatedAt())
                    .paymentExpiresAt(ExpireUnpaidOrdersService.paymentExpiresAt(order))
                    .paymentMethod(resolvePaymentMethod(order))
                    .canRetryPayment(canRetryPayment(order))
                    .firstProductImage(firstImage)
                    .build();
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/{orderCode}/ship")
    public ResponseEntity<Void> shipOrder(@PathVariable String orderCode) {
        updateOrderStatusUseCase.shipOrder(orderCode);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/admin/{orderCode}/deliver")
    public ResponseEntity<Void> deliverOrder(@PathVariable String orderCode) {
        updateOrderStatusUseCase.deliverOrder(orderCode);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{orderCode}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable String orderCode) {
        UUID userId = currentUserProvider.getCurrentUserId();
        updateOrderStatusUseCase.cancelOrder(orderCode, userId);
        return ResponseEntity.noContent().build();
    }

    private String resolveFirstProductImage(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            return null;
        }

        OrderItem firstItem = order.getItems().get(0);
        if (firstItem == null) {
            return null;
        }

        ProductSnapshot productSnapshot = firstItem.getProductSnapshot();
        return productSnapshot != null ? productSnapshot.getImageUrl() : null;
    }

    private boolean canRetryPayment(Order order) {
        return (order.getStatus() == OrderStatus.UNPAID || order.getStatus() == OrderStatus.PAYMENT_FAILED)
                && "vnpay".equalsIgnoreCase(resolvePaymentMethod(order))
                && ExpireUnpaidOrdersService.isPaymentWindowOpen(order, LocalDateTime.now());
    }

    private String resolvePaymentMethod(Order order) {
        return order.getPaymentDetail() != null ? order.getPaymentDetail().getPaymentMethod() : null;
    }

    private String toStatusLabel(Order order) {
        if (order == null || order.getStatus() == null) {
            return "";
        }
        if (order.isCodOrder()) {
            return switch (order.getStatus()) {
                case UNPAID -> "Chờ xác nhận";
                case CONFIRMED, PAID -> "Xác nhận thành công";
                case SHIPPING -> "Đang giao";
                case DELIVERED -> "Hoàn thành";
                case CANCELLED -> "Đã hủy";
                case REFUND_PENDING -> "Chờ hoàn tiền";
                case REFUNDED -> "Đã hoàn tiền";
                case PAYMENT_FAILED -> "Thanh toán thất bại";
                case IN_TRANSIT -> "Đang vận chuyển";
            };
        }
        return switch (order.getStatus()) {
            case CONFIRMED -> "Xác nhận thành công";
            case UNPAID -> "Chờ thanh toán";
            case PAYMENT_FAILED -> "Thanh toán thất bại";
            case PAID -> "Đã thanh toán";
            case IN_TRANSIT -> "Đang vận chuyển";
            case SHIPPING -> "Đang giao";
            case DELIVERED -> "Hoàn thành";
            case CANCELLED -> "Đã hủy";
            case REFUND_PENDING -> "Chờ hoàn tiền";
            case REFUNDED -> "Đã hoàn tiền";
        };
    }

    private PaymentDetailResponse toPaymentDetailResponse(Order order) {
        PaymentDetail paymentDetail = order.getPaymentDetail();
        if (paymentDetail == null) {
            return PaymentDetailResponse.builder()
                    .paymentMethod(null)
                    .paymentStatus(null)
                    .paidAmount(0.0)
                    .paidAt(null)
                    .transactionCode(order.getOrderCode())
                    .build();
        }

        return PaymentDetailResponse.builder()
                .paymentMethod(paymentDetail.getPaymentMethod())
                .paymentStatus(paymentDetail.getPaymentStatus())
                .paidAmount(paymentDetail.getPaidAmount())
                .paidAt(paymentDetail.getPaidAt())
                .transactionCode(order.getOrderCode())
                .build();
    }

    private PaymentTimelineResponse toPaymentTimelineResponse(Order order) {
        PaymentTimeline timeline = order.getPaymentTimeline();
        return PaymentTimelineResponse.builder()
                .orderCreatedAt(timeline != null && timeline.getOrderCreatedAt() != null ? timeline.getOrderCreatedAt() : order.getCreatedAt())
                .paymentInitiatedAt(timeline != null ? timeline.getPaymentInitiatedAt() : null)
                .paymentCompletedAt(timeline != null ? timeline.getPaymentCompletedAt() : null)
                .paymentFailedAt(timeline != null ? timeline.getPaymentFailedAt() : null)
                .paymentExpiresAt(ExpireUnpaidOrdersService.paymentExpiresAt(order))
                .build();
    }
}
