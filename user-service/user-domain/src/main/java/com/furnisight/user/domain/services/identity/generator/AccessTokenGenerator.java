package com.furnisight.user.domain.services.identity.generator;

import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.valueobjects.identity.AccessToken;

public interface AccessTokenGenerator {
    AccessToken generateToken(Account account);
}
