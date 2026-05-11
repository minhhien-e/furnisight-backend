package com.furnisight.user.domain.repository.profile;

import com.furnisight.user.domain.entities.profile.UserProfile;
import com.furnisight.user.domain.valueobjects.profile.PhoneNumber;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository {
    UserProfile save(UserProfile userProfile);

    Optional<UserProfile> findByAccountId(UUID accountId);

    Optional<UserProfile> findById(UUID id);

    boolean existsByPhoneNumber(PhoneNumber phoneNumber);
}
