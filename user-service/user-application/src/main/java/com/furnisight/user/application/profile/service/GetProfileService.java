package com.furnisight.user.application.profile.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.furnisight.user.application.profile.port.in.usecase.GetProfileUseCase;
import com.furnisight.user.domain.entities.profile.UserProfile;
import com.furnisight.user.domain.services.profile.UserProfileLifecycleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetProfileService implements GetProfileUseCase {
    private final UserProfileLifecycleService userProfileLifecycleService;

    @Override
    @Transactional(readOnly = true)
    public UserProfile execute(UUID accountId) {
        return userProfileLifecycleService.getProfile(accountId);
    }
}
