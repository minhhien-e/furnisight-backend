package com.furnisight.catalog.application.product.dto.query;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class GetProductDetailQuery {
    private UUID productId;
}
