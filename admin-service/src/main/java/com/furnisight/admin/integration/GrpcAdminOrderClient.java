package com.furnisight.admin.integration;

import com.furnisight.admin.order.AdminActionResponse;
import com.furnisight.admin.order.AdminOrderServiceGrpc;
import com.furnisight.admin.order.CreateVoucherRequest;
import com.furnisight.admin.order.DeleteVoucherRequest;
import com.furnisight.admin.order.GetAdminOrdersRequest;
import com.furnisight.admin.order.GetAdminVouchersRequest;
import com.furnisight.admin.order.GetRecentOrdersRequest;
import com.furnisight.admin.order.GetRevenueSummaryRequest;
import com.furnisight.admin.order.OrderPageResponse;
import com.furnisight.admin.order.OrderStatsResponse;
import com.furnisight.admin.order.RecentOrderListResponse;
import com.furnisight.admin.order.RevenueSummaryResponse;
import com.furnisight.admin.order.UpdateOrderStatusRequest;
import com.furnisight.admin.order.UpdateVoucherRequest;
import com.furnisight.admin.order.VoucherListResponse;
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

    public VoucherListResponse getVouchers(String query, String status) {
        return adminOrderServiceStub.getAdminVouchers(GetAdminVouchersRequest.newBuilder()
                .setQuery(query == null ? "" : query)
                .setStatus(status == null ? "" : status)
                .build());
    }

    public AdminActionResponse createVoucher(CreateVoucherRequest request) {
        return adminOrderServiceStub.createVoucher(request);
    }

    public AdminActionResponse updateVoucher(UpdateVoucherRequest request) {
        return adminOrderServiceStub.updateVoucher(request);
    }

    public AdminActionResponse deleteVoucher(String id) {
        return adminOrderServiceStub.deleteVoucher(DeleteVoucherRequest.newBuilder()
                .setId(id == null ? "" : id)
                .build());
    }

    public AdminActionResponse updateOrderStatus(UUID adminId, String orderCode, String status) {
        return adminOrderServiceStub.updateOrderStatus(UpdateOrderStatusRequest.newBuilder()
                .setAdminId(adminId == null ? "" : adminId.toString())
                .setOrderCode(orderCode == null ? "" : orderCode)
                .setStatus(status == null ? "" : status)
                .build());
    }

    public RevenueSummaryResponse getRevenueSummary(int months) {
        return adminOrderServiceStub.getRevenueSummary(GetRevenueSummaryRequest.newBuilder()
                .setMonths(months)
                .build());
    }
}
