package com.furnisight.order.application.processing;

import com.furnisight.order.application.payment.handler.PaymentHandlerRegistry;
import com.furnisight.order.application.status.handler.OrderStatusHandlerRegistry;
import com.furnisight.order.application.workflow.OrderAuditLogService;
import com.furnisight.order.application.workflow.OrderHistoryService;
import com.furnisight.order.application.workflow.OrderNotificationService;
import com.furnisight.order.application.workflow.OrderValidationService;
import com.furnisight.order.domain.enums.OrderStatus;
import com.furnisight.order.domain.exceptions.ErrorCode;
import com.furnisight.order.domain.exceptions.ValidationException;
import com.furnisight.order.domain.repository.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderProcessingService {
    private final OrderValidationService validationService;
    private final PaymentHandlerRegistry paymentHandlers;
    private final OrderStatusHandlerRegistry statusHandlers;
    private final OrderHistoryService historyService;
    private final OrderAuditLogService auditLogService;
    private final OrderNotificationService notificationService;
    private final OrderRepository orderRepository;

    @Transactional
    public OrderProcessingResult process(OrderProcessingCommand command) {
        validationService.validate(command);
        OrderProcessingContext context = new OrderProcessingContext(command);

        paymentHandlers.resolve(command.getPaymentType()).process(context);

        if (context.getOrder() == null) {
            throw new ValidationException(ErrorCode.ORDER_NOT_FOUND);
        }
        OrderStatus previousStatus = context.getPreviousStatus();

        updateTrackingCode(context);
        boolean transitioned = applyStatusTransition(context, previousStatus);
        orderRepository.save(context.getOrder());
        if (transitioned) {
            historyService.record(context, previousStatus);
            auditLogService.enqueue(context, previousStatus);
            notificationService.enqueue(context, previousStatus);
        }

        return new OrderProcessingResult(
                context.getOrder(),
                context.isSuccessful(),
                context.getPaymentUrl()
        );
    }

    private boolean applyStatusTransition(OrderProcessingContext context, OrderStatus previousStatus) {
        OrderStatus targetStatus = context.getTargetStatus();
        if (targetStatus == null || targetStatus == previousStatus) {
            return false;
        }

        statusHandlers.resolve(previousStatus).handle(context);
        if (context.getOperation() == OrderOperation.CANCEL
                && (targetStatus == OrderStatus.CANCELLED || targetStatus == OrderStatus.CANCELLED_BY_ADMIN || targetStatus == OrderStatus.REFUND_PENDING)) {
            context.getOrder().recordCancellation();
        }
        return true;
    }

    private void updateTrackingCode(OrderProcessingContext context) {
        String trackingCode = context.getTrackingCode();
        boolean isTrackingCodeMissing = trackingCode == null || trackingCode.isBlank() || "null".equalsIgnoreCase(trackingCode.trim());

        if (context.getTargetStatus() == OrderStatus.SHIPPING && isTrackingCodeMissing) {
            throw new ValidationException(ErrorCode.INVALID_ORDER_STATUS);
        }

        if (isTrackingCodeMissing) {
            return;
        }
        String normalizedTrackingCode = trackingCode.trim();
        if (context.getTargetStatus() != OrderStatus.SHIPPING
                || context.getOrder().getStatus() == OrderStatus.IN_TRANSIT
                || context.getOrder().getStatus() == OrderStatus.DELIVERED) {
            if (normalizedTrackingCode.equals(context.getOrder().getTrackingCode())) {
                return;
            }
            throw new ValidationException(ErrorCode.INVALID_ORDER_STATUS);
        }
        context.getOrder().updateTrackingCode(normalizedTrackingCode);
    }
}
