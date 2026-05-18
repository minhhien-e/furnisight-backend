package com.furnisight.review.core.repository;

import com.furnisight.review.core.model.entity.ReviewProcessingJob;

public interface ReviewJobWritePort {
    void save(ReviewProcessingJob job);
}
