package com.furnisight.user.infrastructure.database.repository.impl.identity;

import com.furnisight.user.domain.entities.identity.SocialAccount;
import com.furnisight.user.domain.enums.identity.SocialProvider;
import com.furnisight.user.domain.repository.identity.SocialAccountRepository;
import com.furnisight.user.infrastructure.database.repository.jpa.identity.SocialAccountJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
@RequiredArgsConstructor
public class SocialAccountRepositoryImpl implements SocialAccountRepository {
    private final SocialAccountJpaRepository socialAccountJpaRepository;
    @Override
    public SocialAccount save(SocialAccount account) {
        return socialAccountJpaRepository.save(account);
    }

    @Override
    public void delete(SocialAccount account) {
        socialAccountJpaRepository.delete(account);
    }

    @Override
    public Optional<SocialAccount> findByProviderUserId(String providerUserId, SocialProvider provider) {
        return socialAccountJpaRepository.findByProviderUserIdAndProvider(providerUserId, provider);
    }
}
