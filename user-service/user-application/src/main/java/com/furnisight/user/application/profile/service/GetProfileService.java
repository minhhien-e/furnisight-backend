package com.furnisight.user.application.profile.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.furnisight.user.application.profile.dto.ProfileResult;
import com.furnisight.user.application.profile.port.in.usecase.GetProfileUseCase;
import com.furnisight.user.application.profile.port.out.ProfileMediaUrlResolver;
import com.furnisight.user.domain.entities.profile.UserProfile;
import com.furnisight.user.domain.services.profile.UserProfileLifecycleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetProfileService implements GetProfileUseCase {

    private final UserProfileLifecycleService userProfileLifecycleService;
    private final ProfileMediaUrlResolver profileMediaUrlResolver;

    @Override
    @Transactional(readOnly = true)
    public ProfileResult execute(UUID accountId) {
        UserProfile profile = userProfileLifecycleService.getProfile(accountId);
        return new ProfileResult(profile, resolveAvatarUrl(profile));
    }

    /**
     * Priority: avatarMediaId (media-service via gRPC) → avatarUrl (OAuth2 provider URL).
     */
    private String resolveAvatarUrl(UserProfile profile) {
        if (profile.getAvatarMediaId() != null) {
            String url = profileMediaUrlResolver.resolveUrl(profile.getAvatarMediaId()).orElse(null);
            if (url != null) return url;
        }
        return profile.getAvatarUrl();
    }
}
