package com.furnisight.catalog.domain.exceptions;

import lombok.Getter;

@Getter
public enum ErrorCode {
    ORDER_NOT_FOUND("Order not found"),
    INVALID_ORDER_STATUS("Invalid order status"),
    ORDER_ITEM_EMPTY("Order must contain at least one item"),
    PRODUCT_ID_EMPTY("Product ID cannot be empty"),
    NEGATIVE_PRICE("Price cannot be negative"),
    INVALID_QUANTITY("Quantity must be greater than zero"),
    INVALID_FEE("Fee cannot be negative"),
    INVALID_DISCOUNT("Discount cannot be negative"),
    INVALID_PAYMENT_AMOUNT("Payment amount cannot be negative"),
    INVALID_PAYMENT_METHOD("Payment method cannot be empty"),
    INVALID_SHIPPING_INFO("Shipping information cannot be empty"),
    INVALID_PRODUCT_INFO("Product information cannot be empty"),
    INSUFFICIENT_STOCK("Insufficient stock for requested product quantity"),
    INVALID_PRODUCT_DIMENSIONS("Product dimensions must be greater than zero"),
    PAYMENT_FAILED("Payment failed"),
    INVALID_SIGNATURE("Invalid signature"),
    UNAUTHORIZED("User not authenticated"),
    RATING_VALUE_MISSING("Rating value missing"),
    PRODUCT_VARIANT_NOT_FOUND("Product variant not found"),
    PRODUCT_NOT_FOUND("Product not found"),
    DUPLICATE_PRODUCT_NAME("Duplicate product name"),
    REVIEW_ALREADY_ARCHIVED("Review already archived"),
    INVALID_RATING("Invalid rating"),
    USER_ID_MISSING("User id missing"),
    REVIEW_IN_PROCESS("Review in process"),
    REVIEW_NOT_FOUND("Review not found"),
    CATEGORY_NOT_FOUND("Category not found"),
    DUPLICATE_CATEGORY_SLUG("Duplicate category slug"),
    INVALID_STOCK_QUANTITY("Invalid stock quantity"),
    INVALID_TITLE_LENGTH("Invalid title length"),
    INVALID_CATEGORY_NAME("Invalid category name"),
    ORDER_ITEM_ID_MISSING("Order item id missing"),
    TITLE_MISSING("Title missing"),
    INVALID_REVIEW_STATE("Invalid review state"),
    INVALID_CATEGORY_SLUG("Invalid category slug"),
    INVALID_PRODUCT_SLUG("Invalid product slug"),
    INVALID_PRICE("Invalid price"),
    DUPLICATE_CATEGORY_NAME("Duplicate category name"),
    PRODUCT_ID_MISSING("Product id missing"),
    INVALID_PRODUCT_DESCRIPTION("Invalid product description"),
    INVALID_PRODUCT_NAME("Invalid product name"),
    INVALID_CONTENT("Invalid content"),
    CONTENT_MISSING("Content missing");

    private final String description;

    ErrorCode(String description) {
        this.description = description;
    }
}
