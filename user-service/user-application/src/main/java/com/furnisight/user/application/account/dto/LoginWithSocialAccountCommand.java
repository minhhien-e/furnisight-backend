package com.furnisight.user.application.account.dto;

import com.furnisight.user.domain.enums.identity.SocialProvider;

public record LoginWithSocialAccountCommand(
    String providerUserId,          // required: provider's unique user ID (e.g. "sub" in OIDC)
    SocialProvider provider,        // required: GOOGLE | FACEBOOK | ...
    String email,                   // optional
    String avatarUrl,               // optional
    String fullName,                // optional: use if firstName/lastName unavailable
    String firstName,               // optional
    String lastName                 // optional
) {
}
