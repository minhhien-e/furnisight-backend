package com.furnisight.review.core.strategy;

import com.furnisight.review.core.model.entity.ReviewProcessingJob;
import com.furnisight.review.core.model.enums.ReviewJobType;

public interface ReviewJobStrategy {
    void execute(ReviewProcessingJob job);
    ReviewJobType getType();
}
