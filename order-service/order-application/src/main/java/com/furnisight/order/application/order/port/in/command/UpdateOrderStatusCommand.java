package com.furnisight.order.application.order.port.in.command;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateOrderStatusCommand {
    private String orderCode;
    private String status;
}
