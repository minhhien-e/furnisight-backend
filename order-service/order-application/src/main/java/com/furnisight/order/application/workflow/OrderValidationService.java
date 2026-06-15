package com.furnisight.order.application.workflow;

import com.furnisight.order.application.processing.OrderOperation;
import com.furnisight.order.application.processing.OrderProcessingCommand;
import com.furnisight.order.domain.exceptions.ErrorCode;
import com.furnisight.order.domain.exceptions.ValidationException;
import org.springframework.stereotype.Service;

@Service
public class OrderValidationService {
    public void validate(OrderProcessingCommand command) {
        if (command == null || command.getOperation() == null || command.getPaymentType() == null) {
            throw new ValidationException(ErrorCode.INVALID_PAYMENT_METHOD);
        }
        if (command.getOperation() != OrderOperation.PROCESS_CALLBACK && command.getOrder() == null) {
            throw new ValidationException(ErrorCode.ORDER_NOT_FOUND);
        }
    }
}
