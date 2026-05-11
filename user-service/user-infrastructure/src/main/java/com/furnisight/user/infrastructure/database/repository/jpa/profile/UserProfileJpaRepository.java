package com.furnisight.user.infrastructure.database.repository.jpa.profile;

import com.furnisight.user.domain.entities.profile.UserProfile;
import com.furnisight.user.domain.valueobjects.profile.PhoneNumber;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileJpaRepository extends JpaRepository<UserProfile, UUID> {
    Optional<UserProfile> findByAccountId(UUID accountId);

    boolean existsByPhoneNumber(PhoneNumber phoneNumber);
}
