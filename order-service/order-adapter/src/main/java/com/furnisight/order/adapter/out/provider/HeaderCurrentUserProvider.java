package com.furnisight.order.adapter.out.provider;

import com.furnisight.order.application.common.port.in.CurrentUserProvider;
import com.furnisight.order.domain.exceptions.ErrorCode;
import com.furnisight.order.domain.exceptions.UnauthorizedException;
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
            throw new UnauthorizedException(ErrorCode.UNAUTHORIZED);
        }

        String principal = auth.getPrincipal().toString();
        if (principal.isBlank() || "anonymousUser".equals(principal)) {
            throw new UnauthorizedException(ErrorCode.UNAUTHORIZED);
        }

        try {
            return UUID.fromString(principal);
        } catch (IllegalArgumentException ex) {
            throw new UnauthorizedException(ErrorCode.UNAUTHORIZED);
        }
    }
}
