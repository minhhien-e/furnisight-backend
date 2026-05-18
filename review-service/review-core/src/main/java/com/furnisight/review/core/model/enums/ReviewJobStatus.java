package com.furnisight.review.core.model.enums;

public enum ReviewJobStatus {
    PENDING,       // Chờ thực thi
    PROCESSING,    // Đang được worker xử lý
    COMPLETED,     // Đã thực thi thành công
    FAILED,        // Thất bại (cần xem errorMessage)
    RETRYING       // Đang trong quá trình retry
}
