package com.furnisight.user.application.profile.service;

import com.furnisight.user.domain.exceptions.identity.ErrorCode;
import com.furnisight.user.domain.exceptions.identity.NotFoundException;
import com.furnisight.user.domain.repository.profile.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.furnisight.user.application.profile.dto.UpdateProfileCommand;
import com.furnisight.user.application.profile.port.in.usecase.UpdateProfileUseCase;
import com.furnisight.user.domain.entities.profile.UserProfile;
import com.furnisight.user.domain.services.profile.UserProfileLifecycleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateProfileService implements UpdateProfileUseCase {
    private final UserProfileLifecycleService userProfileLifecycleService;
    private final UserProfileRepository userProfileRepository;

    @Override
    @Transactional
    public UserProfile execute(UpdateProfileCommand command) {
        UserProfile profile = userProfileRepository.findByAccountId(command.accountId())
            .orElseThrow(() -> new NotFoundException(ErrorCode.PROFILE_NOT_FOUND));
        return userProfileLifecycleService.updateProfile(
            profile,
            command.displayName(),
            command.firstName(),
            command.lastName(),
            command.avatarUrl(),
            command.bio(),
            command.dateOfBirth(),
            command.gender());
    }

}
