package com.furnisight.media.infrastructure.repository.jpa;

import com.furnisight.media.core.model.entity.MediaProcessingJob;
import com.furnisight.media.core.model.enums.ProcessingJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MediaProcessingJobJpaRepository extends JpaRepository<MediaProcessingJob, UUID> {
    Optional<MediaProcessingJob> findByStatusAndAssetId(ProcessingJobStatus status, UUID mediaId);
}
