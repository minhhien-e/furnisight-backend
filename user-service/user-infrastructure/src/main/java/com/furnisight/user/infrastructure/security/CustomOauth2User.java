package com.furnisight.user.infrastructure.security;

import com.furnisight.user.domain.entities.identity.AccountToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

public record CustomOauth2User(OAuth2User oauth2User, AccountToken accountToken) implements OAuth2User, OidcUser {

    @Override
    public Map<String, Object> getAttributes() {
        return oauth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return oauth2User.getAuthorities();
    }

    @Override
    public String getName() {
        return oauth2User.getName();
    }

    @Override
    public Map<String, Object> getClaims() {
        return ((OidcUser) oauth2User).getClaims();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return ((OidcUser) oauth2User).getUserInfo();
    }

    @Override
    public OidcIdToken getIdToken() {
        return ((OidcUser) oauth2User).getIdToken();
    }
}
