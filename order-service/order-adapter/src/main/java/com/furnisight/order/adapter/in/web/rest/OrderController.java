package com.furnisight.order.adapter.in.web.rest;

import com.furnisight.order.application.order.port.in.command.CreateOrderCommand;
import com.furnisight.order.application.order.port.in.usecase.CreateOrderUseCase;
import com.furnisight.order.application.order.port.in.usecase.GetOrderQuery;
import com.furnisight.order.application.order.port.in.usecase.UpdateOrderStatusUseCase;
import com.furnisight.order.domain.entities.order.Order;
import com.furnisight.order.application.order.port.in.dto.OrderResponse;
import com.furnisight.order.adapter.in.web.dto.response.ProductPurchaseCheckResponse;
import com.furnisight.order.domain.repository.order.OrderStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderQuery getOrderQuery;
    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;
    private final com.furnisight.order.application.common.port.in.CurrentUserProvider currentUserProvider;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    @PostMapping("/initiate")
    @PreAuthorize("isAuthenticated() and !hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> initiateOrder(@RequestBody CreateOrderCommand command) {
        command.setUserId(currentUserProvider.getCurrentUserId());
        return ResponseEntity.ok(createOrderUseCase.createOrder(command));
    }

    @GetMapping("/user")
    public ResponseEntity<List<OrderResponse>> getUserOrders() {
        UUID userId = currentUserProvider.getCurrentUserId();
        List<Order> orders = getOrderQuery.getUserOrders(userId);

        return ResponseEntity.ok(orders.stream().map(OrderResponse::summary).toList());
    }

    @GetMapping("/user/products/{productId}/purchased")
    public ResponseEntity<ProductPurchaseCheckResponse> checkPurchasedProduct(@PathVariable String productId) {
        UUID userId = currentUserProvider.getCurrentUserId();
        UUID orderItemId = getOrderQuery.getDeliveredOrderItemIdForProduct(userId, productId).orElse(null);

        return ResponseEntity.ok(ProductPurchaseCheckResponse.builder()
                .purchased(orderItemId != null)
                .orderItemId(orderItemId != null ? orderItemId.toString() : null)
                .build());
    }

    @GetMapping("/{orderCode}")
    public ResponseEntity<OrderResponse> getOrderDetail(@PathVariable String orderCode) {
        Order order = getOrderQuery.getOrderDetail(orderCode);

        return ResponseEntity.ok(OrderResponse.detail(order, orderStatusHistoryRepository.findByOrderId(order.getId())));
    }

    @GetMapping("/admin")
    public ResponseEntity<List<OrderResponse>> getAdminOrders(@RequestParam(required = false) String status) {
        List<Order> orders = getOrderQuery.getAdminOrders(status);

        return ResponseEntity.ok(orders.stream().map(OrderResponse::summary).toList());
    }

    @PostMapping("/admin/{orderCode}/ship")
    public ResponseEntity<Void> shipOrder(@PathVariable String orderCode) {
        updateOrderStatusUseCase.shipOrder(orderCode);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/admin/{orderCode}/deliver")
    public ResponseEntity<Void> deliverOrder(@PathVariable String orderCode) {
        updateOrderStatusUseCase.deliverOrder(orderCode);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{orderCode}/confirm-receive")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> confirmReceive(@PathVariable String orderCode) {
        UUID userId = currentUserProvider.getCurrentUserId();
        updateOrderStatusUseCase.confirmReceive(orderCode, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{orderCode}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable String orderCode) {
        UUID userId = currentUserProvider.getCurrentUserId();
        updateOrderStatusUseCase.cancelOrder(orderCode, userId);
        return ResponseEntity.noContent().build();
    }

}
