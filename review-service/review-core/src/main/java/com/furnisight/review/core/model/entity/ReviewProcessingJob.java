package com.furnisight.review.core.model.entity;

import com.furnisight.review.core.model.enums.ActorType;
import com.furnisight.review.core.model.enums.ModerationReason;
import com.furnisight.review.core.model.enums.ReviewJobStatus;
import com.furnisight.review.core.model.enums.ReviewJobType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "review_processing_job")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewProcessingJob extends BaseEntity {

    @Column(nullable = false)
    private UUID reviewId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewJobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewJobStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private ModerationReason moderationReason;


    /**
     * Loại Actor kích hoạt job (FraudService, Admin, System...)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActorType triggeredBy;

    /**
     * ID của Actor cụ thể (Ví dụ: adminId hoặc serviceName)
     */
    @Column(nullable = false)
    private String triggerActorId;

    /**
     * ID của worker instance đã xử lý job này (Dùng để debug lỗi distribute)
     */
    private String processedByNodeId;


    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    @Builder.Default
    private int retryCount = 0;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;
}
