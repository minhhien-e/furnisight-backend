package com.furnisight.user.application.profile.service;

import com.furnisight.user.application.profile.dto.ProfileResult;
import com.furnisight.user.application.profile.dto.UpdateProfileCommand;
import com.furnisight.user.application.profile.port.in.usecase.UpdateProfileUseCase;
import com.furnisight.user.application.profile.port.out.ProfileMediaUrlResolver;
import com.furnisight.user.domain.entities.profile.UserProfile;
import com.furnisight.user.domain.exceptions.identity.ErrorCode;
import com.furnisight.user.domain.exceptions.identity.NotFoundException;
import com.furnisight.user.domain.repository.profile.UserProfileRepository;
import com.furnisight.user.domain.services.profile.UserProfileLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import com.furnisight.user.domain.events.profile.UserProfileUpdatedEvent;

@Service
@RequiredArgsConstructor
public class UpdateProfileService implements UpdateProfileUseCase {

    private final UserProfileLifecycleService userProfileLifecycleService;
    private final UserProfileRepository userProfileRepository;
    private final ProfileMediaUrlResolver profileMediaUrlResolver;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ProfileResult execute(UpdateProfileCommand command) {
        UserProfile profile = userProfileRepository.findByAccountId(command.accountId())
            .orElseThrow(() -> new NotFoundException(ErrorCode.PROFILE_NOT_FOUND));
        UserProfile updated = userProfileLifecycleService.updateProfile(
            profile,
            command.displayName(),
            command.fullName(),
            command.avatarMediaId(),
            command.bio(),
            command.dateOfBirth(),
            command.gender());
        
        eventPublisher.publishEvent(new UserProfileUpdatedEvent(
            updated.getAccountId(),
            updated.getFullName(),
            updated.getAvatarMediaId()
        ));

        return new ProfileResult(updated, resolveAvatarUrl(updated));
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
