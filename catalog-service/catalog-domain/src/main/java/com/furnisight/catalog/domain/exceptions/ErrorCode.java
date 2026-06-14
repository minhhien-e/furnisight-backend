package com.furnisight.catalog.domain.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // --- CATALOG ERROR CODES ---
    PRODUCT_NOT_FOUND("Product not found"),
    CATEGORY_NOT_FOUND("Category not found"),
    PRODUCT_VARIANT_NOT_FOUND("Product variant not found"),

    DUPLICATE_PRODUCT_NAME("Product name already exists"),
    DUPLICATE_CATEGORY_SLUG("Category slug already exists"),
    DUPLICATE_CATEGORY_NAME("Category name already exists"),
    
    INVALID_PRODUCT_SLUG("Invalid product slug"),
    INVALID_STOCK_QUANTITY("Invalid stock quantity"),
    INSUFFICIENT_STOCK("Insufficient stock"),
    INVALID_PRODUCT_NAME("Invalid product name"),
    INVALID_PRODUCT_DIMENSIONS("Invalid product dimensions"),
    INVALID_PRODUCT_DESCRIPTION("Invalid product description"),
    INVALID_PRICE("Invalid price"),
    INVALID_CATEGORY_SLUG("Invalid category slug"),
    INVALID_CATEGORY_NAME("Invalid category name"),
    INVALID_PRODUCT_STATE("Invalid product state"),

    INVALID_INPUT_DATA("Invalid input data"),
    UNAUTHORIZED_ACCESS("Unauthorized access"),
    INVALID_DATA("Invalid data"),

    // --- REVIEW ERROR CODES ---
    INVALID_VOTE_DATA("Invalid vote data"),
    INVALID_VOTE_TYPE("Unsupported vote type"),
    VOTE_TYPE_UNCHANGED("Vote type remains unchanged"),

    TITLE_MISSING("Title is required"),
    USER_ID_MISSING("User ID is required"),
    RATING_VALUE_MISSING("Rating value is required"),
    ORDER_ITEM_ID_MISSING("Order item ID is required"),
    PRODUCT_ID_MISSING("Product ID is required"),
    CONTENT_MISSING("Content text and hash are required"),
    REVIEW_ID_MISSING("Review ID is required"),
    IP_MISSING("IP address is required"),
    VOTE_ID_MISSING("Vote ID is required"),
    VOTE_TYPE_MISSING("Vote type is required"),

    INVALID_TITLE_LENGTH("Title length is invalid"),
    INVALID_CONTENT("Review content is invalid"),

    REVIEW_NOT_FOUND("Review not found"),
    INVALID_RATING("Rating must be between 1 and 5 stars"),
    REVIEW_IN_PROCESS("This review is currently being processed"),
    REVIEW_ARCHIVED("Review has been archived"),
    INVALID_REVIEW_STATE("Invalid review state"),
    INVALID_IP_ADDRESS("Invalid IP address"),
    REVIEW_ALREADY_ARCHIVED("Review is already archived");

    private final String description;
}
