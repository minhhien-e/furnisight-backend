package com.furnisight.order.application.order.port.in.command;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UpdateOrderStatusCommand {
    private String orderCode;
    private String status;
    private UUID actorId;
    private String actorType;
    private String trackingCode;
    private String note;
}
