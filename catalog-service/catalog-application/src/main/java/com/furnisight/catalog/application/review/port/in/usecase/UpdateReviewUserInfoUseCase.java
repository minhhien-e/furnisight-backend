package com.furnisight.catalog.application.review.port.in.usecase;

import java.util.UUID;

public interface UpdateReviewUserInfoUseCase {
    void updateUserInfo(UUID userId, String userName, UUID userAvatarMediaId);
}
