package com.furnisight.user.infrastructure.messaging;

public final class EventTopics {
    public static final String ACCOUNT_CREATED = "account-created";
    public static final String ACCOUNT_DELETED = "account-deleted";
    public static final String ACCOUNT_VERIFICATION_REQUESTED = "account-verification-requested";
    public static final String ACCOUNT_RESET_PASSWORD_REQUESTED = "account-reset-password-requested";
    public static final String PRODUCT_FAVORITED = "product-favorited";
    public static final String PRODUCT_UNFAVORITED = "product-unfavorited";
    public static final String SOCIAL_ACCOUNT_CREATED = "social-account-created";
    public static final String USER_PROFILE_UPDATED = "user-profile-updated";

    private EventTopics() {
    }
}
