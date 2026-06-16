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
import com.furnisight.admin.order.UpdateVoucherRequest;
import com.furnisight.admin.order.VoucherDto;
import com.furnisight.admin.order.VoucherListResponse;
import com.furnisight.admin.order.GetTopSellingProductsRequest;
import com.furnisight.admin.order.TopSellingProductsResponse;
import com.furnisight.admin.order.TopProductDto;
import com.furnisight.order.application.order.port.in.usecase.UpdateOrderStatusUseCase;
import com.furnisight.order.application.order.port.in.command.UpdateOrderStatusCommand;
import com.furnisight.order.application.promotion.port.out.repository.PromotionRepository;
import com.furnisight.order.domain.entities.order.Order;
import com.furnisight.order.domain.entities.order.OrderItem;
import com.furnisight.order.domain.entities.promotion.Promotion;
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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class AdminOrderGrpcServer extends AdminOrderServiceGrpc.AdminOrderServiceImplBase {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final Map<String, String> STATUS_ALIASES = Map.of("SUCCESS", "DELIVERED");

    private final OrderRepository orderRepository;
    private final PromotionRepository promotionRepository;
    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;

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
            String query = normalizeText(request.getQuery());
            String status = normalizeText(request.getStatus());
            VoucherListResponse response = VoucherListResponse.newBuilder()
                    .addAllVouchers(promotionRepository.findAll().stream()
                            .filter(promotion -> matchesVoucherQuery(promotion, query))
                            .filter(promotion -> matchesVoucherStatus(promotion, status))
                            .sorted(Comparator.comparing(Promotion::getCode, Comparator.nullsLast(String::compareToIgnoreCase)))
                            .map(this::toVoucherDto)
                            .toList())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            log.error("Failed to get admin vouchers", ex);
            responseObserver.onError(ex);
        }
    }

    @Override
    public void createVoucher(CreateVoucherRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        completeAction(responseObserver, () -> {
            String code = requireText(request.getCode(), "Thiếu mã voucher.").toUpperCase(Locale.ROOT);
            if (promotionRepository.findByCode(code).isPresent()) {
                throw new IllegalArgumentException("Mã voucher đã tồn tại.");
            }
            promotionRepository.save(applyVoucher(Promotion.builder().id(UUID.randomUUID()).code(code).build(), request, code));
        }, "Voucher created");
    }

    @Override
    public void updateVoucher(UpdateVoucherRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        completeAction(responseObserver, () -> {
            UUID id = UUID.fromString(request.getId());
            Promotion promotion = promotionRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy voucher."));
            String code = requireText(request.getCode(), "Thiếu mã voucher.").toUpperCase(Locale.ROOT);
            promotionRepository.findByCode(code)
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException("Mã voucher đã tồn tại.");
                    });
            promotionRepository.save(applyVoucher(promotion, request, code));
        }, "Voucher updated");
    }

    @Override
    public void deleteVoucher(DeleteVoucherRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        completeAction(responseObserver, () -> promotionRepository.deleteById(UUID.fromString(request.getId())), "Voucher deleted");
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
    public void getTopSellingProducts(GetTopSellingProductsRequest request, StreamObserver<TopSellingProductsResponse> responseObserver) {
        try {
            int limit = request.getLimit() > 0 ? request.getLimit() : 5;
            List<Object[]> queryResults = orderRepository.findTopSellingProducts(limit);

            TopSellingProductsResponse.Builder builder = TopSellingProductsResponse.newBuilder();
            for (Object[] row : queryResults) {
                String productId = row[0] == null ? "" : row[0].toString();
                String productName = row[1] == null ? "" : row[1].toString();
                String categoryName = row[2] == null ? "" : row[2].toString();
                String imageUrl = row[3] == null ? "" : row[3].toString();
                double price = row[4] == null ? 0D : ((Number) row[4]).doubleValue();
                int soldCount = row[5] == null ? 0 : ((Number) row[5]).intValue();
                double totalRevenue = row[6] == null ? 0D : ((Number) row[6]).doubleValue();

                builder.addProducts(TopProductDto.newBuilder()
                        .setProductId(productId)
                        .setProductName(productName)
                        .setCategoryName(categoryName)
                        .setImageUrl(imageUrl)
                        .setPrice(price)
                        .setSoldCount(soldCount)
                        .setTotalRevenue(totalRevenue)
                        .build());
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
                .setTrackingCode(safe(order.getTrackingCode()))
                .build();
    }

    private OrderStatus parseStatus(String rawStatus) {
        String normalized = normalizeStatus(rawStatus);
        if (normalized.isBlank()) {
            return null;
        }
        return OrderStatus.valueOf(STATUS_ALIASES.getOrDefault(normalized, normalized));
    }

    private String normalizeStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return "";
        }
        return rawStatus.trim()
                .replace('-', '_')
                .replace(' ', '_')
                .toUpperCase();
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
        try {
            return value == null || value.isBlank() ? null : UUID.fromString(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
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
            case "DELIVERED", "SUCCESS" -> "Đã giao";
            case "CANCELLED" -> "Đã hủy";
            case "REFUND_PENDING" -> "Chờ hoàn tiền";
            case "REFUNDED" -> "Đã hoàn tiền";
            case "PAYMENT_FAILED" -> "Thanh toán lỗi";
            default -> "Chờ thanh toán";
        };
    }

    private String safe(String value) {
        return value == null ? "" : value;
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
                .setStatusLabel(voucherStatusLabel(promotion))
                .build();
    }

    private Promotion applyVoucher(Promotion promotion, CreateVoucherRequest request, String code) {
        promotion.setCode(code);
        promotion.setName(requireText(request.getName(), "Thiếu tên voucher."));
        promotion.setDescription(safe(request.getDescription()).trim());
        promotion.setIcon(defaultText(request.getIcon(), "badgePercent"));
        promotion.setDiscountType(toDiscountType(request.getDiscountType()));
        promotion.setDiscountValue(nonNegative(request.getDiscountValue()));
        promotion.setMaxDiscount(nonNegativeOrNull(request.getMaxDiscount()));
        promotion.setMinOrder(nonNegativeOrNull(request.getMinOrder()));
        promotion.setStartDate(parseDateTime(request.getStartDate()));
        promotion.setEndDate(parseDateTime(request.getEndDate()));
        promotion.setActive(request.getActive());
        return promotion;
    }

    private Promotion applyVoucher(Promotion promotion, UpdateVoucherRequest request, String code) {
        promotion.setCode(code);
        promotion.setName(requireText(request.getName(), "Thiếu tên voucher."));
        promotion.setDescription(safe(request.getDescription()).trim());
        promotion.setIcon(defaultText(request.getIcon(), "badgePercent"));
        promotion.setDiscountType(toDiscountType(request.getDiscountType()));
        promotion.setDiscountValue(nonNegative(request.getDiscountValue()));
        promotion.setMaxDiscount(nonNegativeOrNull(request.getMaxDiscount()));
        promotion.setMinOrder(nonNegativeOrNull(request.getMinOrder()));
        promotion.setStartDate(parseDateTime(request.getStartDate()));
        promotion.setEndDate(parseDateTime(request.getEndDate()));
        promotion.setActive(request.getActive());
        return promotion;
    }

    private boolean matchesVoucherQuery(Promotion promotion, String query) {
        return query == null
                || normalizedText(promotion.getCode()).contains(query)
                || normalizedText(promotion.getName()).contains(query);
    }

    private boolean matchesVoucherStatus(Promotion promotion, String status) {
        if (status == null || status.isBlank()) {
            return true;
        }
        return switch (status) {
            case "active" -> promotion.isActive() && !isExpired(promotion);
            case "inactive" -> !promotion.isActive();
            case "expired" -> isExpired(promotion);
            default -> true;
        };
    }

    private String voucherStatusLabel(Promotion promotion) {
        if (!promotion.isActive()) {
            return "Đã tắt";
        }
        return isExpired(promotion) ? "Hết hạn" : "Đang bật";
    }

    private boolean isExpired(Promotion promotion) {
        return promotion.getEndDate() != null && promotion.getEndDate().isBefore(LocalDateTime.now());
    }

    private DiscountType toDiscountType(String value) {
        try {
            return DiscountType.valueOf(defaultText(value, "PERCENT").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Loại giảm giá không hợp lệ.");
        }
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(value);
    }

    private String normalizeText(String value) {
        return value == null || value.isBlank() ? null : value.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizedText(String value) {
        return value == null || value.isBlank() ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String defaultText(String value, String fallback) {
        String text = safe(value).trim();
        return text.isBlank() ? fallback : text;
    }

    private double nonNegative(double value) {
        if (value < 0) {
            throw new IllegalArgumentException("Giá trị giảm không được âm.");
        }
        return value;
    }

    private Double nonNegativeOrNull(double value) {
        if (value < 0) {
            throw new IllegalArgumentException("Giá trị cấu hình không được âm.");
        }
        return value == 0D ? null : value;
    }
}
