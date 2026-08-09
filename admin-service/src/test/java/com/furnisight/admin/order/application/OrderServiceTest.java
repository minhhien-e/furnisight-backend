package com.furnisight.admin.order.application;

import com.furnisight.admin.order.OrderDto;
import com.furnisight.admin.order.OrderPageResponse;
import com.furnisight.admin.order.infrastructure.grpc.AdminOrderGrpcClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    @Test
    void copiesOrderResponseFieldsWithoutPresentationMapping() {
        AdminOrderGrpcClient client = mock(AdminOrderGrpcClient.class);
        when(client.getOrders(1, 20, "SHIPPING", "ORD"))
                .thenReturn(OrderPageResponse.newBuilder()
                        .addOrders(OrderDto.newBuilder()
                                .setId("order-id-1")
                                .setOrderCode("ORD-1")
                                .setCustomer("")
                                .setItemCount(2)
                                .setTotalAmount(500_000)
                                .setStatus("SHIPPING")
                                .setPaymentMethod("vnpay")
                                .setFirstProductImage("https://example.com/image.png")
                                .setTrackingCode("GHN123456")
                                .setCreatedAt("2026-06-10T10:00:00")
                                .build())
                        .setCurrentPage(1)
                        .setTotalPages(1)
                        .setTotalElements(1)
                        .build());

        var response = new OrderService(client).getOrders(1, 20, "SHIPPING", "ORD");

        assertThat(response.items()).singleElement().satisfies(order -> {
            assertThat(order.id()).isEqualTo("order-id-1");
            assertThat(order.orderCode()).isEqualTo("ORD-1");
            assertThat(order.status()).isEqualTo("SHIPPING");
            assertThat(order.createdAt()).isEqualTo("2026-06-10T10:00:00");
            assertThat(order.totalAmount()).isEqualTo(500_000);
            assertThat(order.customer()).isEmpty();
            assertThat(order.itemCount()).isEqualTo(2);
            assertThat(order.firstProductImage()).isEqualTo("https://example.com/image.png");
            assertThat(order.trackingCode()).isEqualTo("GHN123456");
        });
    }

    @Test
    void keepsCodPaidStatusUnchanged() {
        AdminOrderGrpcClient client = mock(AdminOrderGrpcClient.class);
        when(client.getOrders(1, 20, "", ""))
                .thenReturn(OrderPageResponse.newBuilder()
                        .addOrders(OrderDto.newBuilder()
                                .setOrderCode("ORD-COD")
                                .setCustomer("Minh")
                                .setPaymentMethod("cod")
                                .setStatus("PAID")
                                .build())
                        .setCurrentPage(1)
                        .setTotalPages(1)
                        .setTotalElements(1)
                        .build());

        var response = new OrderService(client).getOrders(1, 20, "", "");

        assertThat(response.items()).singleElement().satisfies(order -> {
            assertThat(order.status()).isEqualTo("PAID");
            assertThat(order.paymentMethod()).isEqualTo("cod");
        });
    }

    @Test
    void copiesDeliveredStatusWithoutLabelMapping() {
        AdminOrderGrpcClient client = mock(AdminOrderGrpcClient.class);
        when(client.getOrders(1, 20, "DELIVERED", ""))
                .thenReturn(OrderPageResponse.newBuilder()
                        .addOrders(OrderDto.newBuilder()
                                .setOrderCode("ORD-DONE")
                                .setCustomer("Minh")
                                .setPaymentMethod("cod")
                                .setStatus("DELIVERED")
                                .build())
                        .setCurrentPage(1)
                        .setTotalPages(1)
                        .setTotalElements(1)
                        .build());

        var response = new OrderService(client).getOrders(1, 20, "DELIVERED", "");

        assertThat(response.items()).singleElement().satisfies(order -> {
            assertThat(order.status()).isEqualTo("DELIVERED");
            assertThat(order.customer()).isEqualTo("Minh");
        });
    }
}
