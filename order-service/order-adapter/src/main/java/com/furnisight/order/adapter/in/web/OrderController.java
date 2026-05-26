package com.furnisight.order.adapter.in.web;

import com.furnisight.order.application.order.port.in.command.CreateOrderCommand;
import com.furnisight.order.application.order.port.in.usecase.CreateOrderUseCase;
import com.furnisight.order.application.order.port.in.usecase.GetOrderQuery;
import com.furnisight.order.application.order.port.in.usecase.UpdateOrderStatusUseCase;
import com.furnisight.order.application.order.port.in.command.UpdateOrderStatusCommand;
import com.furnisight.order.adapter.in.web.dto.request.UpdateOrderStatusRequest;
import com.furnisight.order.domain.entities.order.Order;
import com.furnisight.order.adapter.in.web.dto.response.OrderListResponse;
import com.furnisight.order.adapter.in.web.dto.response.OrderDetailResponse;
import com.furnisight.order.adapter.in.web.dto.response.OrderItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderQuery getOrderQuery;
    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;

    @PostMapping
    public ResponseEntity<UUID> createOrder(@RequestBody CreateOrderCommand command) {
        UUID orderId = createOrderUseCase.createOrder(command);
        return ResponseEntity.ok(orderId);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderListResponse>> getUserOrders(@PathVariable UUID userId) {
        List<Order> orders = getOrderQuery.getUserOrders(userId);

        List<OrderListResponse> response = orders.stream().map(order -> {
            String firstImage = null;
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                firstImage = order.getItems().get(0).getProductSnapshot().getImageUrl();
            }
            return OrderListResponse.builder()
                    .id(order.getId())
                    .orderCode(order.getOrderCode())
                    .status(order.getStatus().name())
                    .totalAmount(order.getTotalAmount())
                    .createdAt(order.getCreatedAt())
                    .firstProductImage(firstImage)
                    .build();
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderCode}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(@PathVariable String orderCode) {
        Order order = getOrderQuery.getOrderDetail(orderCode);

        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .productSnapshot(item.getProductSnapshot())
                        .price(item.getPrice())
                        .oldPrice(item.getOldPrice())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        OrderDetailResponse response = OrderDetailResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .status(order.getStatus().name())
                .subTotal(order.getSubTotal())
                .totalAmount(order.getTotalAmount())
                .savedAmount(order.getSavedAmount())
                .customerNote(order.getCustomerNote())
                .fee(order.getFee())
                .shippingDetail(order.getShippingDetail())
                .paymentDetail(order.getPaymentDetail())
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin")
    public ResponseEntity<List<OrderListResponse>> getAdminOrders(@RequestParam(required = false) String status) {
        List<Order> orders = getOrderQuery.getAdminOrders(status);

        List<OrderListResponse> response = orders.stream().map(order -> {
            String firstImage = null;
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                firstImage = order.getItems().get(0).getProductSnapshot().getImageUrl();
            }
            return OrderListResponse.builder()
                    .id(order.getId())
                    .orderCode(order.getOrderCode())
                    .status(order.getStatus().name())
                    .totalAmount(order.getTotalAmount())
                    .createdAt(order.getCreatedAt())
                    .firstProductImage(firstImage)
                    .build();
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/admin/{orderCode}/status")
    public ResponseEntity<Void> updateOrderStatus(
            @PathVariable String orderCode, 
            @RequestBody UpdateOrderStatusRequest request) {
        
        UpdateOrderStatusCommand command = UpdateOrderStatusCommand.builder()
                .orderCode(orderCode)
                .status(request.getStatus())
                .build();
        
        updateOrderStatusUseCase.updateOrderStatus(command);
        return ResponseEntity.noContent().build();
    }
}
