package com.furnisight.media.core.repository;

import com.furnisight.media.core.model.entity.MediaProcessingJob;
import com.furnisight.media.core.model.enums.ProcessingJobStatus;
import java.util.UUID;

public interface MediaProcessingJobRepository {
    MediaProcessingJob findById(UUID id);
    MediaProcessingJob save(MediaProcessingJob job);
    MediaProcessingJob findByStatusAndAssetId(ProcessingJobStatus status, UUID mediaId);
}
