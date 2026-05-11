package com.furnisight.user.domain.repository.identity;

import com.furnisight.user.domain.entities.identity.SocialAccount;
import com.furnisight.user.domain.enums.identity.SocialProvider;

import java.util.Optional;

public interface SocialAccountRepository {
    SocialAccount save(SocialAccount account);

    void delete(SocialAccount account);

    Optional<SocialAccount> findByProviderUserId(String providerUserId, SocialProvider provider);
}
