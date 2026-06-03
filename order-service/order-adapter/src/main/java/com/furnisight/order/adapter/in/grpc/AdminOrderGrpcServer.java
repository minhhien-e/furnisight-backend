package com.furnisight.order.adapter.in.grpc;

import com.furnisight.admin.order.AdminActionResponse;
import com.furnisight.admin.order.AdminOrderServiceGrpc;
import com.furnisight.admin.order.ChartPoint;
import com.furnisight.admin.order.CreateVoucherRequest;
import com.furnisight.admin.order.DeleteVoucherRequest;
import com.furnisight.admin.order.DeliverOrderRequest;
import com.furnisight.admin.order.GetAdminOrdersRequest;
import com.furnisight.admin.order.GetAdminVouchersRequest;
import com.furnisight.admin.order.GetRecentOrdersRequest;
import com.furnisight.admin.order.OrderDto;
import com.furnisight.admin.order.OrderPageResponse;
import com.furnisight.admin.order.OrderStatsResponse;
import com.furnisight.admin.order.OrderStatusCount;
import com.furnisight.admin.order.RecentOrderListResponse;
import com.furnisight.admin.order.ShipOrderRequest;
import com.furnisight.admin.order.UpdateOrderStatusRequest;
import com.furnisight.admin.order.UpdateVoucherRequest;
import com.furnisight.admin.order.VoucherDto;
import com.furnisight.admin.order.VoucherListResponse;
import com.furnisight.order.adapter.out.repository.promotion.jpa.PromotionJpaRepository;
import com.furnisight.order.application.order.port.in.usecase.UpdateOrderStatusUseCase;
import com.furnisight.order.domain.entities.promotion.Promotion;
import com.furnisight.order.domain.entities.order.Order;
import com.furnisight.order.domain.entities.order.OrderItem;
import com.furnisight.order.domain.enums.DiscountType;
import com.furnisight.order.domain.enums.OrderStatus;
import com.furnisight.order.domain.repository.order.OrderRepository;
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

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class AdminOrderGrpcServer extends AdminOrderServiceGrpc.AdminOrderServiceImplBase {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final OrderRepository orderRepository;
    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;
    private final PromotionJpaRepository promotionJpaRepository;

    @Override
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
                            .setLabel(toStatusLabel(status.name()))
                            .setCount(orderRepository.countByStatus(status))
                            .build())
                    .forEach(builder::addOrdersByStatus);

            Arrays.stream(OrderStatus.values())
                    .map(status -> OrderStatusCount.newBuilder()
                            .setStatus(status.name())
                            .setLabel(toStatusLabel(status.name()))
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
    public void getAdminVouchers(GetAdminVouchersRequest request, StreamObserver<VoucherListResponse> responseObserver) {
        try {
            String query = request.getQuery() == null ? "" : request.getQuery().trim().toLowerCase();
            String status = request.getStatus() == null ? "" : request.getStatus().trim().toLowerCase();
            List<VoucherDto> vouchers = promotionJpaRepository.findAll().stream()
                    .filter(promotion -> query.isBlank()
                            || safe(promotion.getCode()).toLowerCase().contains(query)
                            || safe(promotion.getName()).toLowerCase().contains(query))
                    .filter(promotion -> status.isBlank()
                            || ("active".equals(status) && promotion.isActive())
                            || ("inactive".equals(status) && !promotion.isActive()))
                    .map(this::toVoucherDto)
                    .toList();
            responseObserver.onNext(VoucherListResponse.newBuilder().addAllVouchers(vouchers).build());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            log.error("Failed to get admin vouchers", ex);
            responseObserver.onError(ex);
        }
    }

    @Override
    public void createVoucher(CreateVoucherRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        completeAction(responseObserver, () -> {
            Promotion promotion = Promotion.builder()
                    .id(java.util.UUID.randomUUID())
                    .code(normalizeCode(request.getCode()))
                    .name(defaultText(request.getName(), normalizeCode(request.getCode())))
                    .description(safe(request.getDescription()))
                    .icon(safe(request.getIcon()))
                    .discountType(parseDiscountType(request.getDiscountType()))
                    .discountValue(Math.max(request.getDiscountValue(), 0D))
                    .maxDiscount(optionalPositive(request.getMaxDiscount()))
                    .minOrder(optionalPositive(request.getMinOrder()))
                    .startDate(parseDateTime(request.getStartDate()))
                    .endDate(parseDateTime(request.getEndDate()))
                    .active(request.getActive())
                    .build();
            promotionJpaRepository.save(promotion);
        }, "Voucher created");
    }

    @Override
    public void updateVoucher(UpdateVoucherRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        completeAction(responseObserver, () -> {
            Promotion promotion = promotionJpaRepository.findById(java.util.UUID.fromString(request.getId()))
                    .orElseThrow(() -> new IllegalArgumentException("Voucher not found"));
            promotion.setCode(normalizeCode(request.getCode()));
            promotion.setName(defaultText(request.getName(), promotion.getCode()));
            promotion.setDescription(safe(request.getDescription()));
            promotion.setIcon(safe(request.getIcon()));
            promotion.setDiscountType(parseDiscountType(request.getDiscountType()));
            promotion.setDiscountValue(Math.max(request.getDiscountValue(), 0D));
            promotion.setMaxDiscount(optionalPositive(request.getMaxDiscount()));
            promotion.setMinOrder(optionalPositive(request.getMinOrder()));
            promotion.setStartDate(parseDateTime(request.getStartDate()));
            promotion.setEndDate(parseDateTime(request.getEndDate()));
            promotion.setActive(request.getActive());
            promotionJpaRepository.save(promotion);
        }, "Voucher updated");
    }

    @Override
    public void deleteVoucher(DeleteVoucherRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        completeAction(responseObserver, () -> promotionJpaRepository.deleteById(java.util.UUID.fromString(request.getId())),
                "Voucher deleted");
    }

    @Override
    public void shipOrder(ShipOrderRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        completeAction(responseObserver, () -> updateOrderStatusUseCase.shipOrder(request.getOrderCode()), "Order moved to shipping");
    }

    @Override
    public void deliverOrder(DeliverOrderRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        completeAction(responseObserver, () -> updateOrderStatusUseCase.deliverOrder(request.getOrderCode()), "Order delivered");
    }

    @Override
    public void updateOrderStatus(UpdateOrderStatusRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        String status = normalizeStatus(request.getStatus());
        if ("SHIPPING".equals(status)) {
            completeAction(responseObserver, () -> updateOrderStatusUseCase.shipOrder(request.getOrderCode()), "Order moved to shipping");
            return;
        }
        if ("DELIVERED".equals(status) || "SUCCESS".equals(status)) {
            completeAction(responseObserver, () -> updateOrderStatusUseCase.deliverOrder(request.getOrderCode()), "Order delivered");
            return;
        }
        responseObserver.onNext(AdminActionResponse.newBuilder()
                .setSuccess(false)
                .setMessage("Unsupported admin order status: " + request.getStatus())
                .build());
        responseObserver.onCompleted();
    }

    private void completeAction(StreamObserver<AdminActionResponse> responseObserver, Runnable action, String message) {
        try {
            action.run();
            responseObserver.onNext(AdminActionResponse.newBuilder().setSuccess(true).setMessage(message).build());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            log.error("Failed to update order status", ex);
            responseObserver.onError(ex);
        }
    }

    private OrderDto toOrderDto(Order order) {
        return OrderDto.newBuilder()
                .setId(safe(order.getOrderCode()))
                .setOrderCode(safe(order.getOrderCode()))
                .setStatus(order.getStatus() == null ? "" : order.getStatus().name())
                .setTotalAmount(order.getTotalAmount() == null ? 0D : order.getTotalAmount())
                .setCreatedAt(order.getCreatedAt() == null ? "" : order.getCreatedAt().toString())
                .setPaymentMethod(order.getPaymentDetail() == null ? "" : safe(order.getPaymentDetail().getPaymentMethod()))
                .setFirstProductImage(resolveFirstProductImage(order))
                .setCustomer(resolveCustomer(order))
                .setItemCount(resolveItemCount(order))
                .build();
    }

    private OrderStatus parseStatus(String rawStatus) {
        String normalized = normalizeStatus(rawStatus);
        if (normalized.isBlank()) {
            return null;
        }
        return OrderStatus.valueOf(normalized);
    }

    private String normalizeStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return "";
        }
        String lower = rawStatus.trim().toLowerCase();
        if (lower.contains("giao")) {
            return "SHIPPING";
        }
        if (lower.contains("hoàn") || lower.contains("thành công") || lower.contains("success")) {
            return "DELIVERED";
        }
        if (lower.contains("hủy") || lower.contains("cancel")) {
            return "CANCELLED";
        }
        return rawStatus.trim()
                .replace('-', '_')
                .replace(' ', '_')
                .toUpperCase();
    }

    private String resolveFirstProductImage(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            return "";
        }
        ProductSnapshot productSnapshot = order.getItems().get(0).getProductSnapshot();
        return productSnapshot == null ? "" : safe(productSnapshot.getImageUrl());
    }

    private int resolveItemCount(Order order) {
        if (order.getItems() == null) {
            return 0;
        }
        return order.getItems().stream().mapToInt(item -> item.getQuantity() == null ? 0 : item.getQuantity()).sum();
    }

    private String resolveCustomer(Order order) {
        if (order.getShippingDetail() == null) {
            return "";
        }
        String fullName = order.getShippingDetail().getShippingAddressName();
        return fullName == null ? "" : fullName;
    }

    private String toStatusLabel(String status) {
        return switch (normalizeStatus(status)) {
            case "PAID" -> "Đã thanh toán";
            case "SHIPPING" -> "Đang giao";
            case "DELIVERED", "SUCCESS" -> "Hoàn tất";
            case "CANCELLED" -> "Đã hủy";
            case "PAYMENT_FAILED" -> "Thanh toán lỗi";
            default -> "Chờ thanh toán";
        };
    }

    private VoucherDto toVoucherDto(Promotion promotion) {
        return VoucherDto.newBuilder()
                .setId(promotion.getId() == null ? "" : promotion.getId().toString())
                .setCode(safe(promotion.getCode()))
                .setName(safe(promotion.getName()))
                .setDescription(safe(promotion.getDescription()))
                .setIcon(safe(promotion.getIcon()))
                .setDiscountType(promotion.getDiscountType() == null ? "" : promotion.getDiscountType().name())
                .setDiscountValue(promotion.getDiscountValue() == null ? 0D : promotion.getDiscountValue())
                .setMaxDiscount(promotion.getMaxDiscount() == null ? 0D : promotion.getMaxDiscount())
                .setMinOrder(promotion.getMinOrder() == null ? 0D : promotion.getMinOrder())
                .setStartDate(promotion.getStartDate() == null ? "" : promotion.getStartDate().toString())
                .setEndDate(promotion.getEndDate() == null ? "" : promotion.getEndDate().toString())
                .setActive(promotion.isActive())
                .setStatusLabel(promotion.isActive() ? "Đang bật" : "Đã tắt")
                .build();
    }

    private DiscountType parseDiscountType(String raw) {
        if (raw == null || raw.isBlank()) {
            return DiscountType.FIXED;
        }
        String normalized = raw.trim().replace('-', '_').replace(' ', '_').toUpperCase();
        if ("PERCENTAGE".equals(normalized)) {
            return DiscountType.PERCENT;
        }
        return DiscountType.valueOf(normalized);
    }

    private LocalDateTime parseDateTime(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(raw);
        } catch (Exception ignored) {
            return LocalDate.parse(raw).atStartOfDay();
        }
    }

    private Double optionalPositive(double value) {
        return value > 0 ? value : null;
    }

    private String normalizeCode(String raw) {
        return safe(raw).trim().toUpperCase();
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
