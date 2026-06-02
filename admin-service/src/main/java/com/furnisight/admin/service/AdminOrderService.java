package com.furnisight.admin.service;

import com.furnisight.admin.controller.dto.AdminActionResultResponse;
import com.furnisight.admin.controller.dto.AdminOrderPageResponse;
import com.furnisight.admin.controller.dto.AdminOrderResponse;
import com.furnisight.admin.controller.dto.DashboardRecentOrderResponse;
import com.furnisight.admin.integration.GrpcAdminOrderClient;
import com.furnisight.admin.order.AdminActionResponse;
import com.furnisight.admin.order.OrderDto;
import com.furnisight.admin.order.OrderPageResponse;
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
public class AdminOrderService {

    private final GrpcAdminOrderClient grpcAdminOrderClient;

    public AdminOrderPageResponse getOrders(int page, int size, String status, String query) {
        OrderPageResponse response = grpcAdminOrderClient.getOrders(page, size, status, query);
        return new AdminOrderPageResponse(
                response.getOrdersList().stream().map(this::toOrderResponse).toList(),
                response.getTotalPages(),
                response.getTotalElements(),
                response.getCurrentPage());
    }

    public List<DashboardRecentOrderResponse> getRecentOrders(int limit) {
        return grpcAdminOrderClient.getRecentOrders(limit).getOrdersList().stream()
                .map(this::toDashboardRecentOrder)
                .toList();
    }

    public AdminActionResultResponse updateOrderStatus(UUID adminId, String orderCode, String status) {
        AdminActionResponse response = grpcAdminOrderClient.updateOrderStatus(adminId, orderCode, status);
        return new AdminActionResultResponse(response.getSuccess(), response.getMessage());
    }

    private AdminOrderResponse toOrderResponse(OrderDto order) {
        return new AdminOrderResponse(
                order.getOrderCode().isBlank() ? order.getId() : order.getOrderCode(),
                emptyFallback(order.getCustomer(), "Khách hàng"),
                order.getItemCount(),
                order.getTotalAmount(),
                toOrderTone(order.getStatus()),
                toOrderStatusLabel(order.getStatus()),
                formatDate(order.getCreatedAt()));
    }

    private DashboardRecentOrderResponse toDashboardRecentOrder(OrderDto order) {
        return new DashboardRecentOrderResponse(
                order.getOrderCode().isBlank() ? order.getId() : order.getOrderCode(),
                emptyFallback(order.getCustomer(), "Khách hàng"),
                formatCurrency(order.getTotalAmount()),
                toOrderTone(order.getStatus()),
                toOrderStatusLabel(order.getStatus()));
    }

    private String toOrderTone(String status) {
        return switch (normalizeStatus(status)) {
            case "SHIPPING" -> "shipping";
            case "DELIVERED", "SUCCESS" -> "success";
            case "CANCELLED", "PAYMENT_FAILED" -> "cancel";
            default -> "pending";
        };
    }

    private String toOrderStatusLabel(String status) {
        return switch (normalizeStatus(status)) {
            case "PAID" -> "Đã thanh toán";
            case "SHIPPING" -> "Đang giao";
            case "DELIVERED", "SUCCESS" -> "Hoàn tất";
            case "CANCELLED" -> "Đã hủy";
            case "PAYMENT_FAILED" -> "Thanh toán lỗi";
            default -> "Chờ xác nhận";
        };
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "";
        }
        return status.trim().replace('-', '_').replace(' ', '_').toUpperCase(Locale.ROOT);
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
        NumberFormat formatter = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
        return formatter.format(value) + "đ";
    }

    private String emptyFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
