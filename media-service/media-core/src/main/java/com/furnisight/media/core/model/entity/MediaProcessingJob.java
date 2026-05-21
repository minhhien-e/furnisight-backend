package com.furnisight.media.core.model.entity;

import com.furnisight.media.core.model.enums.ProcessingJobStatus;
import com.furnisight.media.core.model.enums.ProcessingJobType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Entity
@Table
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaProcessingJob extends BaseEntity {

    /**
     * Asset cần xử lý
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private MediaAsset asset;

    /**
     * Loại job
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessingJobType jobType;

    /**
     * Trạng thái job
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessingJobStatus status;

    /**
     * Số lần retry
     */
    @Column(nullable = false)
    private int retryCount = 0;

    /**
     * Error message (nếu fail)
     */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

}
