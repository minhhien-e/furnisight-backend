package com.furnisight.admin.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class HeaderUserProvider implements CurrentUserProvider {

    @Override
    public UUID getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof String) {
            return UUID.fromString((String) principal);
        }
        throw new IllegalStateException("User ID not found in security context");
    }
}
