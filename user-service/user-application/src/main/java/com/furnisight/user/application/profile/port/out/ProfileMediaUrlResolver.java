package com.furnisight.user.application.profile.port.out;

import java.util.Optional;
import java.util.UUID;

/**
 * Port out: resolve a stored media asset to a public URL via the media-service.
 */
public interface ProfileMediaUrlResolver {
    Optional<String> resolveUrl(UUID mediaId);
}
