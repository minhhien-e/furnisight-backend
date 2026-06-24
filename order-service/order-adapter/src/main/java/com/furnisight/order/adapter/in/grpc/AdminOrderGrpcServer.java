package com.furnisight.order.adapter.in.grpc;

import com.furnisight.admin.order.AdminActionResponse;
import com.furnisight.admin.order.AdminOrderServiceGrpc;
import com.furnisight.admin.order.ChartPoint;
import com.furnisight.admin.order.DeliverOrderRequest;
import com.furnisight.admin.order.GetAdminOrdersRequest;
import com.furnisight.admin.order.GetRecentOrdersRequest;
import com.furnisight.admin.order.GetRevenueSummaryRequest;
import com.furnisight.admin.order.MonthlyRevenue;
import com.furnisight.admin.order.OrderDto;
import com.furnisight.admin.order.OrderPageResponse;
import com.furnisight.admin.order.OrderStatsResponse;
import com.furnisight.admin.order.OrderStatusCount;
import com.furnisight.admin.order.RecentOrderListResponse;
import com.furnisight.admin.order.RevenueSummaryResponse;
import com.furnisight.admin.order.ShipOrderRequest;
import com.furnisight.admin.order.UpdateOrderStatusRequest;
import com.furnisight.admin.order.GetTopSellingProductsRequest;
import com.furnisight.admin.order.TopSellingProductsResponse;
import com.furnisight.admin.order.TopProductDto;
import com.furnisight.order.application.order.port.in.usecase.UpdateOrderStatusUseCase;
import com.furnisight.order.application.order.port.in.command.UpdateOrderStatusCommand;
import com.furnisight.order.domain.entities.order.Order;
import com.furnisight.order.domain.entities.order.OrderItem;
import com.furnisight.order.domain.enums.OrderStatus;
import com.furnisight.order.domain.repository.order.OrderRepository;
import com.furnisight.order.domain.repository.order.TopSellingProductQuery;
import com.furnisight.order.domain.valueobjects.ProductSnapshot;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class AdminOrderGrpcServer extends AdminOrderServiceGrpc.AdminOrderServiceImplBase {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final OrderRepository orderRepository;
    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;

    @Override
    @Transactional(readOnly = true)
    public void getAdminOrders(GetAdminOrdersRequest request, StreamObserver<OrderPageResponse> responseObserver) {
        try {
            int page = Math.max(request.getPage() - 1, 0);
            int size = request.getSize() > 0 ? request.getSize() : DEFAULT_PAGE_SIZE;
            OrderStatus status = parseStatus(request.getStatus());

            List<Order> orders = status == null
                    ? orderRepository.findAll(page, size)
                    : orderRepository.findAllByStatus(status, page, size);
            long totalElements = status == null ? orderRepository.countAll() : orderRepository.countByStatus(status);
            int totalPages = size <= 0 ? 0 : (int) Math.ceil((double) totalElements / size);

            OrderPageResponse response = OrderPageResponse.newBuilder()
                    .addAllOrders(orders.stream().map(this::toOrderDto).toList())
                    .setCurrentPage(page + 1)
                    .setTotalElements(totalElements)
                    .setTotalPages(totalPages)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            log.error("Failed to get admin orders", ex);
            responseObserver.onError(ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void getRecentOrders(GetRecentOrdersRequest request, StreamObserver<RecentOrderListResponse> responseObserver) {
        try {
            int limit = request.getLimit() > 0 ? request.getLimit() : 5;
            RecentOrderListResponse response = RecentOrderListResponse.newBuilder()
                    .addAllOrders(orderRepository.findAll(0, limit).stream().map(this::toOrderDto).toList())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            log.error("Failed to get recent orders", ex);
            responseObserver.onError(ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void getOrderStats(com.google.protobuf.Empty request, StreamObserver<OrderStatsResponse> responseObserver) {
        try {
            LocalDate today = LocalDate.now();
            LocalDate firstDayOfMonth = today.withDayOfMonth(1);
            LocalDateTime todayStart = today.atStartOfDay();
            LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();
            LocalDateTime monthStart = firstDayOfMonth.atStartOfDay();
            LocalDateTime nextMonthStart = firstDayOfMonth.plusMonths(1).atStartOfDay();

            OrderStatsResponse.Builder builder = OrderStatsResponse.newBuilder()
                    .setTotalRevenue(orderRepository.sumTotalAmount())
                    .setRevenueThisMonth(orderRepository.sumTotalAmountCreatedAtBetween(monthStart, nextMonthStart))
                    .setTotalOrders(orderRepository.countAll())
                    .setOrdersToday(orderRepository.countCreatedAtBetween(todayStart, tomorrowStart));

            Arrays.stream(OrderStatus.values())
                    .map(status -> OrderStatusCount.newBuilder()
                            .setStatus(status.name())
                            .setCount(orderRepository.countByStatus(status))
                            .build())
                    .forEach(builder::addOrdersByStatus);

            Arrays.stream(OrderStatus.values())
                    .map(status -> OrderStatusCount.newBuilder()
                            .setStatus(status.name())
                            .setCount(orderRepository.countByStatusCreatedAtBetween(status, monthStart, nextMonthStart))
                            .build())
                    .forEach(builder::addOrdersThisMonthByStatus);

            for (int i = 5; i >= 0; i--) {
                LocalDate bucket = today.minusMonths(i).withDayOfMonth(1);
                LocalDateTime start = bucket.atStartOfDay();
                LocalDateTime end = bucket.plusMonths(1).atStartOfDay();
                builder.addRevenueChart(ChartPoint.newBuilder()
                        .setLabel(bucket.format(DATE_FORMAT))
                        .setValue(orderRepository.sumTotalAmountCreatedAtBetween(start, end))
                        .build());
            }

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            log.error("Failed to get order stats", ex);
            responseObserver.onError(ex);
        }
    }


    @Override
    public void shipOrder(ShipOrderRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        completeAction(responseObserver, () -> updateOrderStatusUseCase.updateOrderStatus(
                adminStatusCommand(request.getAdminId(), request.getOrderCode(), OrderStatus.SHIPPING)
        ), "Order moved to shipping");
    }

    @Override
    public void deliverOrder(DeliverOrderRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        completeAction(responseObserver, () -> updateOrderStatusUseCase.updateOrderStatus(
                adminStatusCommand(request.getAdminId(), request.getOrderCode(), OrderStatus.DELIVERED)
        ), "Order delivered");
    }

    @Override
    public void updateOrderStatus(UpdateOrderStatusRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        OrderStatus status = parseStatus(request.getStatus());
        completeAction(responseObserver, () -> updateOrderStatusUseCase.updateOrderStatus(
                adminStatusCommand(
                        request.getAdminId(),
                        request.getOrderCode(),
                        status,
                        request.getTrackingCode(),
                        request.getNote()
                )
        ), "Order status updated");
    }

    @Override
    @Transactional(readOnly = true)
    public void getRevenueSummary(GetRevenueSummaryRequest request, StreamObserver<RevenueSummaryResponse> responseObserver) {
        try {
            int months = request.getMonths() > 0 ? Math.min(request.getMonths(), 24) : 12;
            LocalDate today = LocalDate.now();
            LocalDate firstDayOfMonth = today.withDayOfMonth(1);

            RevenueSummaryResponse.Builder builder = RevenueSummaryResponse.newBuilder()
                    .setTotalRevenue(orderRepository.sumTotalAmount())
                    .setTotalOrders(orderRepository.countAll());

            // Revenue & order count this month
            LocalDateTime monthStart = firstDayOfMonth.atStartOfDay();
            LocalDateTime nextMonthStart = firstDayOfMonth.plusMonths(1).atStartOfDay();
            double revenueThisMonth = orderRepository.sumTotalAmountCreatedAtBetween(monthStart, nextMonthStart);
            long ordersThisMonth = orderRepository.countCreatedAtBetween(monthStart, nextMonthStart);
            builder.setRevenueThisMonth(revenueThisMonth).setOrdersThisMonth(ordersThisMonth);

            // Build monthly breakdown (oldest first)
            double prevRevenue = -1;
            for (int i = months - 1; i >= 0; i--) {
                LocalDate bucket = firstDayOfMonth.minusMonths(i);
                LocalDateTime start = bucket.atStartOfDay();
                LocalDateTime end = bucket.plusMonths(1).atStartOfDay();

                double revenue = orderRepository.sumTotalAmountCreatedAtBetween(start, end);
                long orderCount = orderRepository.countCreatedAtBetween(start, end);

                double momChangePct = 0;
                if (prevRevenue > 0) {
                    momChangePct = Math.round(((revenue - prevRevenue) / prevRevenue) * 10000D) / 100D;
                } else if (prevRevenue == 0 && revenue > 0) {
                    momChangePct = 100D;
                }
                prevRevenue = revenue;

                String yearMonth = bucket.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                String label = "T" + bucket.getMonthValue() + "/" + bucket.getYear();

                builder.addMonthly(MonthlyRevenue.newBuilder()
                        .setYearMonth(yearMonth)
                        .setLabel(label)
                        .setRevenue(revenue)
                        .setOrderCount(orderCount)
                        .setMomChangePct(momChangePct)
                        .build());
            }

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            log.error("Failed to get revenue summary", ex);
            responseObserver.onError(ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void getTopSellingProducts(GetTopSellingProductsRequest request, StreamObserver<TopSellingProductsResponse> responseObserver) {
        try {
            int limit = request.getLimit() > 0 ? request.getLimit() : 5;
            List<TopSellingProductQuery> queryResults = orderRepository.findTopSellingProducts(limit);

            TopSellingProductsResponse.Builder builder = TopSellingProductsResponse.newBuilder();
            for (TopSellingProductQuery product : queryResults) {
                TopProductDto.Builder productBuilder = TopProductDto.newBuilder();
                if (product.productId() != null) {
                    productBuilder.setProductId(product.productId());
                }
                if (product.productName() != null) {
                    productBuilder.setProductName(product.productName());
                }
                if (product.categoryName() != null) {
                    productBuilder.setCategoryName(product.categoryName());
                }
                if (product.imageUrl() != null) {
                    productBuilder.setImageUrl(product.imageUrl());
                }
                if (product.price() != null) {
                    productBuilder.setPrice(product.price());
                }
                if (product.soldCount() != null) {
                    productBuilder.setSoldCount(product.soldCount());
                }
                if (product.totalRevenue() != null) {
                    productBuilder.setTotalRevenue(product.totalRevenue());
                }
                builder.addProducts(productBuilder.build());
            }

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            log.error("Failed to get top selling products", ex);
            responseObserver.onError(ex);
        }
    }

    private void completeAction(StreamObserver<AdminActionResponse> responseObserver, Runnable action, String message) {
        try {
            action.run();
            responseObserver.onNext(AdminActionResponse.newBuilder().setSuccess(true).setMessage(message).build());
            responseObserver.onCompleted();
        } catch (com.furnisight.order.domain.exceptions.ValidationException ex) {
            log.error("Validation error: {}", ex.getMessage());
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription(ex.getMessage()).asRuntimeException());
        } catch (Exception ex) {
            log.error("Failed to update order status", ex);
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription("Internal server error").asRuntimeException());
        }
    }

    private OrderDto toOrderDto(Order order) {
        OrderDto.Builder builder = OrderDto.newBuilder();
        if (order.getId() != null) {
            builder.setId(order.getId().toString());
        }
        if (order.getOrderCode() != null) {
            builder.setOrderCode(order.getOrderCode());
        }
        if (order.getStatus() != null) {
            builder.setStatus(order.getStatus().name());
        }
        if (order.getTotalAmount() != null) {
            builder.setTotalAmount(order.getTotalAmount());
        }
        if (order.getCreatedAt() != null) {
            builder.setCreatedAt(order.getCreatedAt().toString());
        }
        if (order.getPaymentDetail() != null && order.getPaymentDetail().getPaymentMethod() != null) {
            builder.setPaymentMethod(order.getPaymentDetail().getPaymentMethod());
        }
        String firstProductImage = resolveFirstProductImage(order);
        if (firstProductImage != null) {
            builder.setFirstProductImage(firstProductImage);
        }
        String customer = resolveCustomer(order);
        if (customer != null) {
            builder.setCustomer(customer);
        }
        if (order.getItems() != null) {
            builder.setItemCount(resolveItemCount(order));
        }
        if (order.getTrackingCode() != null) {
            builder.setTrackingCode(order.getTrackingCode());
        }
        return builder.build();
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

    private String resolveFirstProductImage(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            return null;
        }
        ProductSnapshot productSnapshot = order.getItems().get(0).getProductSnapshot();
        return productSnapshot == null ? null : productSnapshot.getImageUrl();
    }

    private int resolveItemCount(Order order) {
        if (order.getItems() == null) {
            return 0;
        }
        return order.getItems().stream().mapToInt(item -> item.getQuantity() == null ? 0 : item.getQuantity()).sum();
    }

    private String resolveCustomer(Order order) {
        if (order.getShippingDetail() == null) {
            return null;
        }
        return order.getShippingDetail().getShippingAddressName();
    }

}
