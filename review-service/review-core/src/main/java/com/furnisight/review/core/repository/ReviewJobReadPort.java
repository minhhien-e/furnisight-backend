package com.furnisight.review.core.repository;

import com.furnisight.review.core.model.entity.ReviewProcessingJob;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewJobReadPort {
    Optional<ReviewProcessingJob> findById(UUID id);
    List<ReviewProcessingJob> findPendingJobs(int limit);
}
