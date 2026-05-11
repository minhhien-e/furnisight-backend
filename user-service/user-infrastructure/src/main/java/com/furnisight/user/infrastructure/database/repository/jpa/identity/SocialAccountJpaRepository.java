package com.furnisight.user.infrastructure.database.repository.jpa.identity;

import com.furnisight.user.domain.entities.identity.SocialAccount;
import com.furnisight.user.domain.enums.identity.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SocialAccountJpaRepository extends JpaRepository<SocialAccount, UUID> {
    Optional<SocialAccount> findByProviderUserIdAndProvider(String providerUserId, SocialProvider provider);

    boolean existsByProviderUserIdAndProvider(String providerUserId, SocialProvider provider);
}
