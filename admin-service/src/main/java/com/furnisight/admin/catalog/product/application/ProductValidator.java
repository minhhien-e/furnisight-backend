package com.furnisight.admin.catalog.product.application;

import com.furnisight.admin.catalog.product.web.dto.request.UpsertProductVariantRequest;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class ProductValidator {

    public void validateVariants(List<UpsertProductVariantRequest> variants) {
        if (variants == null || variants.isEmpty()) {
            throw new IllegalArgumentException("Sản phẩm phải có ít nhất một variant");
        }
        Set<String> skus = new HashSet<>();
        for (UpsertProductVariantRequest variant : variants) {
            String sku = normalizeSku(variant.sku());
            if (!skus.add(sku)) {
                throw new IllegalArgumentException("SKU variant bị trùng: " + sku);
            }
            validThreshold(variant.lowStockThreshold());
        }
    }

    public String normalizeSku(String sku) {
        String normalized = value(sku).trim().toUpperCase(Locale.ROOT);
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("SKU variant là bắt buộc");
        }
        return normalized;
    }

    public int validThreshold(int threshold) {
        int value = threshold == 0 ? 5 : threshold;
        if (value < 1 || value > 9999) {
            throw new IllegalArgumentException("Ngưỡng cảnh báo phải từ 1 đến 9999");
        }
        return value;
    }

    private String value(String value) {
        return value == null ? "" : value;
    }
}
