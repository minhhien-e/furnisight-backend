package com.furnisight.catalog.domain.exceptions;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // Product Errors
    PRODUCT_NOT_FOUND("Product not found"),
    PRODUCT_OWNERSHIP_DENIED("Product does not belong to the requested shop"),
    INVALID_PRODUCT_STATE("Operation not allowed in the current product state"),
    INSUFFICIENT_STOCK("Insufficient stock quantity"),
    DUPLICATE_PRODUCT_NAME("Product name already exists"),
    MISSING_PRODUCT_VARIANT("Product must have at least one variant"),
    
    // Category Errors
    CATEGORY_NOT_FOUND("Category not found"),
    DUPLICATE_CATEGORY_NAME("Category name already exists in this parent"),
    DUPLICATE_CATEGORY_SLUG("Category slug already exists"),
    
    // Validation Errors
    INVALID_PRODUCT_NAME("Invalid product name"),
    INVALID_PRODUCT_DESCRIPTION("Invalid product description"),
    INVALID_PRODUCT_DIMENSIONS("Invalid product dimensions"),
    INVALID_SKU("Invalid SKU format"),
    INVALID_PRICE("Invalid product price"),
    INVALID_STOCK_QUANTITY("Invalid stock quantity"),
    INVALID_CATEGORY_NAME("Invalid category name"),
    INVALID_CATEGORY_SLUG("Invalid category slug"),
    
    // Infrastructure/Internal
    OUTBOX_EVENT_PROCESSING_FAILED("Failed to process outbox event");

    private final String description;

    ErrorCode(String description) {
        this.description = description;
    }
}
