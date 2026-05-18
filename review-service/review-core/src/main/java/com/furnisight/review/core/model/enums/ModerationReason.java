package com.furnisight.review.core.model.enums;

public enum ModerationReason {
    NONE,
    SPAM_PATTERN,       // Fraud Service flag
    PROFANITY,          // Từ ngữ thô tục
    SENSITIVE_CONTENT,  // Thông tin cá nhân/nhạy cảm
    EXTORTION_ATTEMPT,  // Tống tiền seller (Edit 1 sao -> 5 sao)
    FALSE_POSITIVE_RECOVERY // Minh oan
}
