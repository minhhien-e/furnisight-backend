package com.furnisight.promotion.adapter.in.web.provider;

import com.furnisight.promotion.application.common.CurrentUserProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class HeaderCurrentUserProvider implements CurrentUserProvider {
    @Override
    public UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new IllegalArgumentException("Unauthorized");
        }
        return UUID.fromString(auth.getPrincipal().toString());
    }
}
