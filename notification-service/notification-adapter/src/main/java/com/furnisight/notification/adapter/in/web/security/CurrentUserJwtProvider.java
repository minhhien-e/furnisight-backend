package com.furnisight.notification.adapter.in.web.security;

import com.furnisight.notification.application.common.port.in.CurrentUserProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CurrentUserJwtProvider implements CurrentUserProvider {

    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            String sub = jwtToken.getToken().getSubject();
            if (sub != null) {
                return UUID.fromString(sub);
            }
        }
        throw new IllegalStateException("Current user is not authenticated or token does not contain a subject");
    }
}
