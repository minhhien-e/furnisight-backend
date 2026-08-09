package com.furnisight.admin.order.application;

import com.furnisight.admin.order.AdminActionResponse;
import com.furnisight.admin.order.OrderDto;
import com.furnisight.admin.order.infrastructure.grpc.AdminOrderGrpcClient;
import com.furnisight.admin.order.web.dto.response.OrderResponse;
import com.furnisight.admin.shared.web.ActionResultResponse;
import com.furnisight.admin.shared.web.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final AdminOrderGrpcClient orderClient;

    public PageResponse<OrderResponse> getOrders(int page, int size, String status, String query) {
        com.furnisight.admin.order.OrderPageResponse response =
                orderClient.getOrders(page, size, status, query);
        return new PageResponse<>(
                response.getOrdersList().stream().map(this::toOrderResponse).toList(),
                response.getTotalPages(), response.getTotalElements(), response.getCurrentPage());
    }

    public List<OrderResponse> getRecentOrders(int limit) {
        return orderClient.getRecentOrders(limit).getOrdersList().stream()
                .map(this::toOrderResponse)
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
                order.getId(),
                order.getOrderCode(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getPaymentMethod(),
                order.getFirstProductImage(),
                order.getCustomer(),
                order.getItemCount(),
                order.getTrackingCode());
    }

}
