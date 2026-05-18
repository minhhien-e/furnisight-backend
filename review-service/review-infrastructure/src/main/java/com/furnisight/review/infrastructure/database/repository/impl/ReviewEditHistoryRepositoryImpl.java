package com.furnisight.review.infrastructure.database.repository.impl;

import com.furnisight.review.core.repository.ReviewEditHistoryRepository;
import com.furnisight.review.core.model.entity.ReviewEditHistory;
import com.furnisight.review.infrastructure.database.repository.jpa.ReviewEditHistoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReviewEditHistoryRepositoryImpl implements ReviewEditHistoryRepository {

    private final ReviewEditHistoryJpaRepository jpaRepository;

    @Override
    public void save(ReviewEditHistory history) {
        jpaRepository.save(history);
    }
}
