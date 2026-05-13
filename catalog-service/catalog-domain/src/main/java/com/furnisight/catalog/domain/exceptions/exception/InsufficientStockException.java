package com.furnisight.catalog.domain.exceptions;

import com.furnisight.catalog.domain.exceptions.enums.ErrorCode;

import java.util.Map;

public class InsufficientStockException extends BaseException {
    public InsufficientStockException(String sku, int currentStock, int requestedQuantity) {
        super(
                ErrorCode.INSUFFICIENT_STOCK,
                "Không đủ số lượng cho SKU: " + sku + ". Tồn kho: " + currentStock + ", Yêu cầu: " + requestedQuantity,
                Map.of("sku", sku, "currentStock", currentStock, "requestedQuantity", requestedQuantity));
    }
}
