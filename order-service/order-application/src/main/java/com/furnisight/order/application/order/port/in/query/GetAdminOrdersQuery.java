package com.furnisight.order.application.order.port.in.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetAdminOrdersQuery {
    private int page;
    private int size;
    private String status;
}
