package com.furnisight.user.infrastructure.messaging;

public final class EventTopics {
    public static final String ACCOUNT_CREATED = "account-created";
    public static final String ACCOUNT_DELETED = "account-deleted";
    public static final String ACCOUNT_VERIFICATION_REQUESTED = "account-verification-requested";
    public static final String ACCOUNT_RESET_PASSWORD_REQUESTED = "account-reset-password-requested";
    public static final String PHONE_CHANGE_OTP_REQUESTED = "phone-change-otp-requested";
    public static final String PHONE_LINK_OTP_REQUESTED = "phone-link-otp-requested";
    public static final String VERIFY_CURRENT_EMAIL_OTP_REQUESTED = "verify-current-email-otp-requested";
    public static final String VERIFY_CURRENT_PHONE_OTP_REQUESTED = "verify-current-phone-otp-requested";
    public static final String PRODUCT_FAVORITED = "product-favorited";
    public static final String PRODUCT_UNFAVORITED = "product-unfavorited";
    public static final String SOCIAL_ACCOUNT_CREATED = "social-account-created";

    private EventTopics() {
    }
}
