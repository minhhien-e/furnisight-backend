package com.furnisight.user.infrastructure.database.repository.impl.profile;

import com.furnisight.user.domain.entities.profile.UserProfile;
import com.furnisight.user.domain.repository.profile.UserProfileRepository;
import com.furnisight.user.domain.valueobjects.profile.PhoneNumber;
import com.furnisight.user.infrastructure.database.repository.jpa.profile.UserProfileJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserProfileRepositoryImpl implements UserProfileRepository {
    private final UserProfileJpaRepository userProfileJpaRepository;

    @Override
    public UserProfile save(UserProfile userProfile) {
        return userProfileJpaRepository.save(userProfile);
    }

    @Override
    public Optional<UserProfile> findByAccountId(UUID accountId) {
        return userProfileJpaRepository.findByAccountId(accountId);
    }

    @Override
    public Optional<UserProfile> findById(UUID id) {
        return userProfileJpaRepository.findById(id);
    }

    @Override
    public boolean existsByPhoneNumber(PhoneNumber phoneNumber) {
        return userProfileJpaRepository.existsByPhoneNumber(phoneNumber);
    }
}
