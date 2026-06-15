package com.furnisight.admin.order.application;

import com.furnisight.admin.order.AdminActionResponse;
import com.furnisight.admin.order.OrderDto;
import com.furnisight.admin.order.infrastructure.grpc.AdminOrderGrpcClient;
import com.furnisight.admin.order.web.dto.response.OrderPageResponse;
import com.furnisight.admin.order.web.dto.response.OrderResponse;
import com.furnisight.admin.order.web.dto.response.RecentOrderResponse;
import com.furnisight.admin.shared.web.ActionResultResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final AdminOrderGrpcClient orderClient;

    public OrderPageResponse getOrders(int page, int size, String status, String query) {
        com.furnisight.admin.order.OrderPageResponse response =
                orderClient.getOrders(page, size, status, query);
        return new OrderPageResponse(
                response.getOrdersList().stream().map(this::toOrderResponse).toList(),
                response.getTotalPages(), response.getTotalElements(), response.getCurrentPage());
    }

    public List<RecentOrderResponse> getRecentOrders(int limit) {
        return orderClient.getRecentOrders(limit).getOrdersList().stream()
                .map(this::toRecentOrderResponse)
                .toList();
    }

    public ActionResultResponse updateOrderStatus(
            UUID adminId, String orderCode, String status, String trackingCode, String note
    ) {
        AdminActionResponse response = orderClient.updateOrderStatus(
                adminId, orderCode, status, trackingCode, note
        );
        return new ActionResultResponse(response.getSuccess(), response.getMessage());
    }

    private OrderResponse toOrderResponse(OrderDto order) {
        return new OrderResponse(
                order.getOrderCode().isBlank() ? order.getId() : order.getOrderCode(),
                emptyFallback(order.getCustomer(), "Khách hàng"), order.getItemCount(),
                order.getTotalAmount(), toOrderTone(order.getStatus()),
                toOrderStatusLabel(order.getStatus()), formatDate(order.getCreatedAt()),
                order.getPaymentMethod());
    }

    private RecentOrderResponse toRecentOrderResponse(OrderDto order) {
        return new RecentOrderResponse(
                order.getOrderCode().isBlank() ? order.getId() : order.getOrderCode(),
                emptyFallback(order.getCustomer(), "Khách hàng"),
                formatCurrency(order.getTotalAmount()), toOrderTone(order.getStatus()),
                toOrderStatusLabel(order.getStatus()));
    }

    private String toOrderTone(String status) {
        return switch (normalizeStatus(status)) {
            case "SHIPPING" -> "shipping";
            case "DELIVERED", "SUCCESS", "REFUNDED" -> "success";
            case "CANCELLED", "PAYMENT_FAILED" -> "cancel";
            case "REFUND_PENDING" -> "pending";
            default -> "pending";
        };
    }

    private String toOrderStatusLabel(String status) {
        return switch (normalizeStatus(status)) {
            case "PAID" -> "Đã thanh toán";
            case "SHIPPING" -> "Đang giao";
            case "DELIVERED", "SUCCESS" -> "Hoàn tất";
            case "CANCELLED" -> "Đã hủy";
            case "REFUND_PENDING" -> "Chờ hoàn tiền";
            case "REFUNDED" -> "Đã hoàn tiền";
            case "PAYMENT_FAILED" -> "Thanh toán lỗi";
            default -> "Chờ xác nhận";
        };
    }

    private String normalizeStatus(String status) {
        return status == null || status.isBlank() ? ""
                : status.trim().replace('-', '_').replace(' ', '_').toUpperCase(Locale.ROOT);
    }

    private String formatDate(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        try {
            return LocalDateTime.parse(value).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception ignored) {
            return value;
        }
    }

    private String formatCurrency(double value) {
        return NumberFormat.getInstance(Locale.forLanguageTag("vi-VN")).format(value) + "đ";
    }

    private String emptyFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
