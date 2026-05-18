package com.furnisight.review.core.exception.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_REVIEW_STATE("1001", "Invalid review state for this action"),
    REVIEW_ALREADY_ARCHIVED("1002", "Cannot perform actions on an archived review"),
    INVALID_INPUT_DATA("1003", "Invalid input data"),
    INVALID_IP_ADDRESS("1004", "Invalid IP address"),
    INVALID_CONTENT("1005", "Invalid review content"),
    INVALID_TITLE_LENGTH("1006", "Title is too long, maximum 255 characters"),

    INVALID_VOTE_DATA("1007", "Invalid vote data"),
    INVALID_VOTE_TYPE("1008", "Unsupported vote type"),
    VOTE_TYPE_UNCHANGED("1009", "Vote type remains unchanged"),

    TITLE_MISSING("1010", "Title is required"),
    USER_ID_MISSING("1011", "User ID is required"),
    RATING_VALUE_MISSING("1012", "Rating value is required"),
    ORDER_ITEM_ID_MISSING("1013", "Order item ID is required"),
    PRODUCT_ID_MISSING("1014", "Product ID is required"),
    CONTENT_MISSING("1015", "Content text and hash are required"),
    REVIEW_ID_MISSING("1016", "Review ID is required"),
    IP_MISSING("1017", "IP address is required"),
    VOTE_ID_MISSING("1018", "Vote ID is required"),
    VOTE_TYPE_MISSING("1019", "Vote type is required"),

    REVIEW_NOT_FOUND("1020", "Review not found"),
    INVALID_RATING("1021", "Rating must be between 1 and 5 stars"),
    REVIEW_IN_PROCESS("1022", "This review is currently being processed"),
    REVIEW_ARCHIVED("1023", "Review has been archived");

    private final String code;
    private final String message;
}
