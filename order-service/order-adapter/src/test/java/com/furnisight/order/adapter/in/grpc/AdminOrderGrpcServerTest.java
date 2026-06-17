package com.furnisight.order.adapter.in.grpc;

import com.furnisight.admin.order.GetAdminOrdersRequest;
import com.furnisight.admin.order.AdminActionResponse;
import com.furnisight.admin.order.OrderPageResponse;
import com.furnisight.admin.order.UpdateOrderStatusRequest;
import com.furnisight.order.application.order.port.in.command.UpdateOrderStatusCommand;
import com.furnisight.order.application.order.port.in.usecase.UpdateOrderStatusUseCase;
import com.furnisight.order.domain.entities.order.Order;
import com.furnisight.order.domain.entities.order.OrderItem;
import com.furnisight.order.domain.enums.OrderStatus;
import com.furnisight.order.domain.repository.order.OrderRepository;
import com.furnisight.order.domain.valueobjects.PaymentDetail;
import com.furnisight.order.domain.valueobjects.ProductSnapshot;
import com.furnisight.order.domain.valueobjects.ShippingDetail;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AdminOrderGrpcServerTest {

    @Test
    void returnsRawOrderDtoFieldsFromDomain() {
        UUID orderId = UUID.randomUUID();
        Order order = order(orderId);
        TestObserver<OrderPageResponse> observer = new TestObserver<>();

        new AdminOrderGrpcServer(new FakeOrderRepository(List.of(order)), new NoopUpdateOrderStatusUseCase())
                .getAdminOrders(GetAdminOrdersRequest.newBuilder()
                        .setPage(1)
                        .setSize(20)
                        .build(), observer);

        assertThat(observer.error).isNull();
        assertThat(observer.completed).isTrue();
        assertThat(observer.value.getOrdersList()).singleElement().satisfies(dto -> {
            assertThat(dto.getId()).isEqualTo(orderId.toString());
            assertThat(dto.getOrderCode()).isEqualTo("ORD-RAW");
            assertThat(dto.getStatus()).isEqualTo("PAID");
            assertThat(dto.getTotalAmount()).isEqualTo(125_000D);
            assertThat(dto.getCreatedAt()).isEqualTo(order.getCreatedAt().toString());
            assertThat(dto.getPaymentMethod()).isEqualTo("vnpay");
            assertThat(dto.getCustomer()).isEqualTo("Minh");
            assertThat(dto.getItemCount()).isEqualTo(2);
            assertThat(dto.getTrackingCode()).isEqualTo("TRACK-1");
            assertThat(dto.getFirstProductImage()).isEqualTo("https://example.com/chair.png");
        });
    }

    @Test
    void invalidStatusIsReturnedAsError() {
        TestObserver<OrderPageResponse> observer = new TestObserver<>();

        new AdminOrderGrpcServer(new FakeOrderRepository(List.of()), new NoopUpdateOrderStatusUseCase())
                .getAdminOrders(GetAdminOrdersRequest.newBuilder()
                        .setStatus("SUCCESS")
                        .build(), observer);

        assertThat(observer.value).isNull();
        assertThat(observer.completed).isFalse();
        assertThat(observer.error).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void emptyAdminIdIsReturnedAsError() {
        TestObserver<AdminActionResponse> observer = new TestObserver<>();

        new AdminOrderGrpcServer(new FakeOrderRepository(List.of()), new NoopUpdateOrderStatusUseCase())
                .updateOrderStatus(UpdateOrderStatusRequest.newBuilder()
                        .setOrderCode("ORD-RAW")
                        .setStatus("PAID")
                        .build(), observer);

        assertThat(observer.value).isNull();
        assertThat(observer.completed).isFalse();
        assertThat(observer.error).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("adminId cannot be empty");
    }

    private Order order(UUID orderId) {
        OrderItem item = OrderItem.builder()
                .id(UUID.randomUUID())
                .productSnapshot(ProductSnapshot.builder()
                        .productId("product-1")
                        .variantId("variant-1")
                        .productName("Chair")
                        .imageUrl("https://example.com/chair.png")
                        .build())
                .price(62_500D)
                .quantity(2)
                .build();
        Order order = Order.builder()
                .id(orderId)
                .orderCode("ORD-RAW")
                .status(OrderStatus.PAID)
                .customerEmail("minh@example.com")
                .shippingDetail(ShippingDetail.builder()
                        .shippingAddressName("Minh")
                        .shippingAddressPhone("0900000000")
                        .shippingAddressDetail("HCM")
                        .build())
                .paymentDetail(PaymentDetail.builder()
                        .paymentMethod("vnpay")
                        .paymentStatus("PAID")
                        .paidAmount(125_000D)
                        .paidAt(LocalDateTime.parse("2026-06-10T10:00:00"))
                        .build())
                .items(List.of(item))
                .build();
        order.setTotalAmount(125_000D);
        order.setTrackingCode("TRACK-1");
        return order;
    }

    private static class TestObserver<T> implements StreamObserver<T> {
        private T value;
        private Throwable error;
        private boolean completed;

        @Override
        public void onNext(T value) {
            this.value = value;
        }

        @Override
        public void onError(Throwable error) {
            this.error = error;
        }

        @Override
        public void onCompleted() {
            this.completed = true;
        }
    }

    private record FakeOrderRepository(List<Order> orders) implements OrderRepository {
        @Override public Order save(Order order) { throw new UnsupportedOperationException(); }
        @Override public Optional<Order> findById(UUID id) { throw new UnsupportedOperationException(); }
        @Override public Optional<Order> findByOrderCode(String orderCode) { throw new UnsupportedOperationException(); }
        @Override public List<Order> findAllByUserId(UUID userId) { throw new UnsupportedOperationException(); }
        @Override public List<Order> findAllByStatus(OrderStatus status) { throw new UnsupportedOperationException(); }
        @Override public List<Order> findAllByStatus(OrderStatus status, int page, int size) { return orders.stream().filter(order -> order.getStatus() == status).toList(); }
        @Override public List<Order> findAllByStatusesAndCreatedAtBefore(List<OrderStatus> statuses, LocalDateTime cutoff) { throw new UnsupportedOperationException(); }
        @Override public List<Order> findAll() { throw new UnsupportedOperationException(); }
        @Override public List<Order> findAll(int page, int size) { return orders; }
        @Override public long countAll() { return orders.size(); }
        @Override public long countByStatus(OrderStatus status) { return orders.stream().filter(order -> order.getStatus() == status).count(); }
        @Override public long countCreatedAtBetween(LocalDateTime start, LocalDateTime end) { throw new UnsupportedOperationException(); }
        @Override public long countByStatusCreatedAtBetween(OrderStatus status, LocalDateTime start, LocalDateTime end) { throw new UnsupportedOperationException(); }
        @Override public double sumTotalAmount() { throw new UnsupportedOperationException(); }
        @Override public double sumTotalAmountCreatedAtBetween(LocalDateTime start, LocalDateTime end) { throw new UnsupportedOperationException(); }
        @Override public List<Object[]> findTopSellingProducts(int limit) { throw new UnsupportedOperationException(); }
        @Override public Optional<UUID> findDeliveredOrderItemIdByUserIdAndProductId(UUID userId, String productId) { throw new UnsupportedOperationException(); }
    }

    private static class NoopUpdateOrderStatusUseCase implements UpdateOrderStatusUseCase {
        @Override public void updateOrderStatus(UpdateOrderStatusCommand command) { }
        @Override public void shipOrder(String orderCode) { }
        @Override public void deliverOrder(String orderCode) { }
        @Override public void refundOrder(String orderCode) { }
        @Override public void cancelOrder(String orderCode, UUID userId) { }
    }
}
