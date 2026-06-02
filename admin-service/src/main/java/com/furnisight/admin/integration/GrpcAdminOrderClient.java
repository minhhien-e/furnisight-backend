package com.furnisight.admin.integration;

import com.furnisight.admin.order.AdminActionResponse;
import com.furnisight.admin.order.AdminOrderServiceGrpc;
import com.furnisight.admin.order.GetAdminOrdersRequest;
import com.furnisight.admin.order.GetRecentOrdersRequest;
import com.furnisight.admin.order.OrderPageResponse;
import com.furnisight.admin.order.OrderStatsResponse;
import com.furnisight.admin.order.RecentOrderListResponse;
import com.furnisight.admin.order.UpdateOrderStatusRequest;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GrpcAdminOrderClient {

    @GrpcClient("order-service")
    private AdminOrderServiceGrpc.AdminOrderServiceBlockingStub adminOrderServiceStub;

    public OrderPageResponse getOrders(int page, int size, String status, String query) {
        return adminOrderServiceStub.getAdminOrders(GetAdminOrdersRequest.newBuilder()
                .setPage(page)
                .setSize(size)
                .setStatus(status == null ? "" : status)
                .setQuery(query == null ? "" : query)
                .build());
    }

    public OrderStatsResponse getOrderStats() {
        return adminOrderServiceStub.getOrderStats(com.google.protobuf.Empty.getDefaultInstance());
    }

    public RecentOrderListResponse getRecentOrders(int limit) {
        return adminOrderServiceStub.getRecentOrders(GetRecentOrdersRequest.newBuilder()
                .setLimit(limit)
                .build());
    }

    public AdminActionResponse updateOrderStatus(UUID adminId, String orderCode, String status) {
        return adminOrderServiceStub.updateOrderStatus(UpdateOrderStatusRequest.newBuilder()
                .setAdminId(adminId == null ? "" : adminId.toString())
                .setOrderCode(orderCode == null ? "" : orderCode)
                .setStatus(status == null ? "" : status)
                .build());
    }
}
