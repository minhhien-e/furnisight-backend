package com.furnisight.review.core.model.enums;

public enum ActorType {
    USER,           // Tác giả
    SYSTEM,         // Tiến trình tự động (Auto-approve, Rebuild)
    ADMIN,          // Moderator con người
    POLICY_SERVICE, // Service quản lý chính sách nội dung
    FRAUD_SERVICE   // Bộ máy phát hiện gian lận
}
