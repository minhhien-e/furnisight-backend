package com.furnisight.admin.service;

import com.furnisight.admin.controller.dto.AdminActionResultResponse;
import com.furnisight.admin.controller.dto.AdminOrderPageResponse;
import com.furnisight.admin.controller.dto.AdminOrderResponse;
import com.furnisight.admin.controller.dto.AdminVoucherListResponse;
import com.furnisight.admin.controller.dto.AdminVoucherResponse;
import com.furnisight.admin.controller.dto.DashboardRecentOrderResponse;
import com.furnisight.admin.controller.dto.SaveAdminVoucherRequest;
import com.furnisight.admin.integration.GrpcAdminOrderClient;
import com.furnisight.admin.order.AdminActionResponse;
import com.furnisight.admin.order.CreateVoucherRequest;
import com.furnisight.admin.order.OrderDto;
import com.furnisight.admin.order.OrderPageResponse;
import com.furnisight.admin.order.UpdateVoucherRequest;
import com.furnisight.admin.order.VoucherDto;
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

    public AdminVoucherListResponse getVouchers(String query, String status) {
        return new AdminVoucherListResponse(grpcAdminOrderClient.getVouchers(query, status).getVouchersList().stream()
                .map(this::toVoucherResponse)
                .toList());
    }

    public AdminActionResultResponse createVoucher(SaveAdminVoucherRequest request) {
        return toActionResult(grpcAdminOrderClient.createVoucher(CreateVoucherRequest.newBuilder()
                .setCode(value(request.code()))
                .setName(value(request.name()))
                .setDescription(value(request.description()))
                .setIcon(value(request.icon()))
                .setDiscountType(value(request.discountType()))
                .setDiscountValue(request.discountValue())
                .setMaxDiscount(request.maxDiscount())
                .setMinOrder(request.minOrder())
                .setStartDate(value(request.startDate()))
                .setEndDate(value(request.endDate()))
                .setActive(request.active())
                .build()));
    }

    public AdminActionResultResponse updateVoucher(String id, SaveAdminVoucherRequest request) {
        return toActionResult(grpcAdminOrderClient.updateVoucher(UpdateVoucherRequest.newBuilder()
                .setId(value(id))
                .setCode(value(request.code()))
                .setName(value(request.name()))
                .setDescription(value(request.description()))
                .setIcon(value(request.icon()))
                .setDiscountType(value(request.discountType()))
                .setDiscountValue(request.discountValue())
                .setMaxDiscount(request.maxDiscount())
                .setMinOrder(request.minOrder())
                .setStartDate(value(request.startDate()))
                .setEndDate(value(request.endDate()))
                .setActive(request.active())
                .build()));
    }

    public AdminActionResultResponse deleteVoucher(String id) {
        return toActionResult(grpcAdminOrderClient.deleteVoucher(id));
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

    private AdminVoucherResponse toVoucherResponse(VoucherDto voucher) {
        return new AdminVoucherResponse(
                voucher.getId(),
                voucher.getCode(),
                voucher.getName(),
                voucher.getDescription(),
                voucher.getIcon(),
                voucher.getDiscountType(),
                voucher.getDiscountValue(),
                voucher.getMaxDiscount(),
                voucher.getMinOrder(),
                voucher.getStartDate(),
                voucher.getEndDate(),
                voucher.getActive(),
                voucher.getStatusLabel().isBlank() ? voucherStatusLabel(voucher.getActive(), voucher.getEndDate()) : voucher.getStatusLabel());
    }

    private String voucherStatusLabel(boolean active, String endDate) {
        if (!active) {
            return "Đã tắt";
        }
        if (endDate != null && !endDate.isBlank()) {
            try {
                if (LocalDateTime.parse(endDate).isBefore(LocalDateTime.now())) {
                    return "Hết hạn";
                }
            } catch (Exception ignored) {
                return "Đang bật";
            }
        }
        return "Đang bật";
    }

    private AdminActionResultResponse toActionResult(AdminActionResponse response) {
        return new AdminActionResultResponse(response.getSuccess(), response.getMessage());
    }

    private String value(String value) {
        return value == null ? "" : value;
    }

}
