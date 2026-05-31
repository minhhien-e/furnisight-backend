package com.furnisight.catalog.application.review.service;

import com.furnisight.catalog.application.review.port.in.usecase.UpdateReviewUserInfoUseCase;
import com.furnisight.catalog.application.review.port.out.repository.ReviewWritePort;
import com.furnisight.catalog.domain.entities.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateReviewUserInfoService implements UpdateReviewUserInfoUseCase {

    private final ReviewWritePort reviewWritePort;

    @Override
    @Transactional
    public void updateUserInfo(UUID userId, String userName, UUID userAvatarMediaId) {
        List<Review> reviews = reviewWritePort.findByUserId(userId);
        for (Review review : reviews) {
            review.updateUserInfo(userName, userAvatarMediaId);
            reviewWritePort.save(review);
        }
    }
}
