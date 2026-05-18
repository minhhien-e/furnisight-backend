package com.furnisight.review.infrastructure.repository.persistence.write.jpa.impl;

import com.furnisight.review.core.repository.ReviewEditHistoryRepository;
import com.furnisight.review.core.model.entity.ReviewEditHistory;
import com.furnisight.review.infrastructure.repository.persistence.write.jpa.SpringDataReviewEditHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;


@Repository
@RequiredArgsConstructor
public class ReviewEditHistoryAdapter implements ReviewEditHistoryRepository {

    private final SpringDataReviewEditHistoryRepository jpaRepository;

    @Override
    public void save(ReviewEditHistory history) {
        jpaRepository.save(history);
    }


}
