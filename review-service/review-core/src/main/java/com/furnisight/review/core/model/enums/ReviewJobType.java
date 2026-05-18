package com.furnisight.review.core.model.enums;


/**
 * Các loại tác vụ bất đồng bộ được thực thi bởi Review Service.
 * Mapping trực tiếp với các luồng nghiệp vụ trong TDD.
 */
public enum ReviewJobType {

    /**
     * Thực thi chuyển đổi trạng thái Review (Hidden, Shadow Ban, Visible).
     * Dùng cho Admin/Policy Service hoặc Fraud Service command.
     */
    MODERATION_ACTION,

    /**
     * Đồng bộ Trust Score nhận được từ User Reputation Service (Async Update).
     * Phù hợp với mục 3.3 trong TDD.
     */
    SYNC_TRUST_SCORE,

    /**
     * Thực thi quyền "Right to be forgotten" cho người dùng.
     * Xử lý Anonymize dữ liệu và xóa media trên S3.
     */
    GDPR_ANONYMIZATION,

    /**
     * Rebuild Read Model (Elasticsearch) cho Review.
     * Sử dụng cho Disaster Recovery khi ES gặp sự cố (mục 6.1).
     */
    REBUILD_READ_MODEL
}
