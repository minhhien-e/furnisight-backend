package com.furnisight.user.domain.services.identity.generator;

import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.valueobjects.identity.AccessToken;

import java.util.List;
import com.furnisight.user.domain.entities.identity.Role;

public interface AccessTokenGenerator {
    AccessToken generateToken(Account account, List<Role> roles);
}
