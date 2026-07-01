package com.furnisight.user.domain.services.profile;

import com.furnisight.user.domain.entities.profile.UserProfile;
import com.furnisight.user.domain.enums.profile.Gender;
import com.furnisight.user.domain.exceptions.identity.ErrorCode;
import com.furnisight.user.domain.exceptions.identity.NotFoundException;
import com.furnisight.user.domain.repository.profile.UserProfileRepository;
import com.furnisight.user.domain.valueobjects.identity.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileLifecycleService {

    private final UserProfileRepository userProfileRepository;

    public UserProfile createProfile(UUID accountId, String fullName,
            String email) {
        Email emailVO = email != null ? new Email(email) : null;
        UserProfile newProfile = new UserProfile(accountId, fullName, emailVO);
        return userProfileRepository.save(newProfile);
    }

    public UserProfile createProfile(UUID accountId, String fullName,
            String email, String avatarUrl) {
        Email emailVO = email != null ? new Email(email) : null;
        UserProfile newProfile = new UserProfile(accountId, fullName, emailVO, avatarUrl);
        return userProfileRepository.save(newProfile);
    }

    public UserProfile getProfile(UUID accountId) {
        return userProfileRepository.findByAccountId(accountId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PROFILE_NOT_FOUND));
    }

    public UserProfile updateProfile(UserProfile profile, String displayName, String fullName,
            UUID avatarMediaId, String bio,
            LocalDate dateOfBirth, String gender) {
        Gender genderEnum = (gender != null && !gender.isBlank())
                ? Gender.valueOf(gender.toUpperCase())
                : null;
        profile.updateProfile(displayName, fullName, avatarMediaId, bio, dateOfBirth, genderEnum);
        return profile;
    }
}
