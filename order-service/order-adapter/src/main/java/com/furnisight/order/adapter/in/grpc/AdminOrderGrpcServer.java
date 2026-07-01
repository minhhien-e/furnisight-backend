package com.furnisight.order.adapter.in.grpc;

import com.furnisight.admin.order.*;
import com.furnisight.order.application.order.port.in.command.UpdateOrderStatusCommand;
import com.furnisight.order.application.order.port.in.dto.admin.AdminOrderPageResult;
import com.furnisight.order.application.order.port.in.dto.admin.OrderStatsResult;
import com.furnisight.order.application.order.port.in.dto.admin.RevenueSummaryResult;
import com.furnisight.order.application.order.port.in.query.GetAdminOrdersQuery;
import com.furnisight.order.application.order.port.in.query.GetOrderStatsQuery;
import com.furnisight.order.application.order.port.in.query.GetRecentOrdersQuery;
import com.furnisight.order.application.order.port.in.query.GetRevenueSummaryQuery;
import com.furnisight.order.application.order.port.in.query.GetTopSellingProductsQuery;
import com.furnisight.order.application.order.port.in.usecase.GetAdminOrdersUseCase;
import com.furnisight.order.application.order.port.in.usecase.GetOrderStatsUseCase;
import com.furnisight.order.application.order.port.in.usecase.GetRecentOrdersUseCase;
import com.furnisight.order.application.order.port.in.usecase.GetRevenueSummaryUseCase;
import com.furnisight.order.application.order.port.in.usecase.GetTopSellingProductsUseCase;
import com.furnisight.order.application.order.port.in.usecase.UpdateOrderStatusUseCase;
import com.furnisight.order.domain.entities.order.Order;
import com.furnisight.order.domain.enums.OrderStatus;
import com.furnisight.order.domain.repository.order.TopSellingProductQuery;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;
import java.util.UUID;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class AdminOrderGrpcServer extends AdminOrderServiceGrpc.AdminOrderServiceImplBase {

    private final GetAdminOrdersUseCase getAdminOrdersUseCase;
    private final GetRecentOrdersUseCase getRecentOrdersUseCase;
    private final GetOrderStatsUseCase getOrderStatsUseCase;
    private final GetRevenueSummaryUseCase getRevenueSummaryUseCase;
    private final GetTopSellingProductsUseCase getTopSellingProductsUseCase;
    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;
    private final AdminOrderGrpcMapper mapper;

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public void getAdminOrders(GetAdminOrdersRequest request, StreamObserver<OrderPageResponse> responseObserver) {
        AdminOrderPageResult result = getAdminOrdersUseCase.getAdminOrders(GetAdminOrdersQuery.builder()
                .page(request.getPage())
                .size(request.getSize())
                .status(request.getStatus())
                .build());
        responseObserver.onNext(mapper.toOrderPageResponse(result));
        responseObserver.onCompleted();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public void getRecentOrders(GetRecentOrdersRequest request, StreamObserver<RecentOrderListResponse> responseObserver) {
        List<Order> orders = getRecentOrdersUseCase.getRecentOrders(GetRecentOrdersQuery.builder()
                .limit(request.getLimit())
                .build());
        RecentOrderListResponse response = RecentOrderListResponse.newBuilder()
                .addAllOrders(orders.stream().map(mapper::toOrderDto).toList())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getOrderStats(com.google.protobuf.Empty request, StreamObserver<OrderStatsResponse> responseObserver) {
        OrderStatsResult result = getOrderStatsUseCase.getOrderStats(GetOrderStatsQuery.builder().build());
        responseObserver.onNext(mapper.toOrderStatsResponse(result));
        responseObserver.onCompleted();
    }

    @Override
    public void shipOrder(ShipOrderRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        updateOrderStatusUseCase.updateOrderStatus(
                adminStatusCommand(request.getAdminId(), request.getOrderCode(), OrderStatus.SHIPPING)
        );
        completeAction(responseObserver, "Order moved to shipping");
    }

    @Override
    public void deliverOrder(DeliverOrderRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        updateOrderStatusUseCase.updateOrderStatus(
                adminStatusCommand(request.getAdminId(), request.getOrderCode(), OrderStatus.DELIVERED)
        );
        completeAction(responseObserver, "Order delivered");
    }

    @Override
    public void updateOrderStatus(UpdateOrderStatusRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        OrderStatus status = parseStatus(request.getStatus());
        updateOrderStatusUseCase.updateOrderStatus(
                adminStatusCommand(
                        request.getAdminId(),
                        request.getOrderCode(),
                        status,
                        request.getTrackingCode(),
                        request.getNote()
                )
        );
        completeAction(responseObserver, "Order status updated");
    }

    @Override
    public void getRevenueSummary(GetRevenueSummaryRequest request, StreamObserver<RevenueSummaryResponse> responseObserver) {
        RevenueSummaryResult result = getRevenueSummaryUseCase.getRevenueSummary(GetRevenueSummaryQuery.builder()
                .months(request.getMonths())
                .build());
        responseObserver.onNext(mapper.toRevenueSummaryResponse(result));
        responseObserver.onCompleted();
    }

    @Override
    public void getTopSellingProducts(GetTopSellingProductsRequest request, StreamObserver<TopSellingProductsResponse> responseObserver) {
        List<TopSellingProductQuery> result = getTopSellingProductsUseCase.getTopSellingProducts(GetTopSellingProductsQuery.builder()
                .limit(request.getLimit())
                .build());
        responseObserver.onNext(mapper.toTopSellingProductsResponse(result));
        responseObserver.onCompleted();
    }

    private void completeAction(StreamObserver<AdminActionResponse> responseObserver, String message) {
        responseObserver.onNext(AdminActionResponse.newBuilder().setSuccess(true).setMessage(message).build());
        responseObserver.onCompleted();
    }

    private OrderStatus parseStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return null;
        }
        return OrderStatus.valueOf(rawStatus.trim().toUpperCase());
    }

    private UpdateOrderStatusCommand adminStatusCommand(String adminId, String orderCode, OrderStatus status) {
        return adminStatusCommand(adminId, orderCode, status, null, null);
    }

    private UpdateOrderStatusCommand adminStatusCommand(
            String adminId, String orderCode, OrderStatus status, String trackingCode, String note
    ) {
        return UpdateOrderStatusCommand.builder()
                .orderCode(orderCode)
                .status(status.name())
                .actorId(parseUuid(adminId))
                .actorType("ADMIN")
                .trackingCode(trackingCode)
                .note(note)
                .build();
    }

    private UUID parseUuid(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("adminId cannot be empty");
        }
        return UUID.fromString(value);
    }
}
