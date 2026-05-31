package com.furnisight.user.application.profile.dto;

import com.furnisight.user.domain.entities.profile.UserProfile;

/**
 * Application-layer output carrying the resolved profile data.
 * avatarUrl is already resolved with priority: avatarMediaId (via media-service) > avatarUrl (OAuth2 provider URL).
 */
public record ProfileResult(
        UserProfile profile,
        String avatarUrl
) {
}
