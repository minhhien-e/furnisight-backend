package com.furnisight.user.domain.exceptions.identity;

import lombok.Getter;

@Getter
public enum ErrorCode {
    ACCOUNT_NOT_FOUND("Account not found"),
    ROLE_NOT_FOUND("Role not found"),
    TOKEN_NOT_FOUND("Token not found"),
    ACCOUNT_ALREADY_EXISTS("Account already exists"),
    ACCOUNT_BANNED("Account is banned"),
    ACCOUNT_TEMPORARILY_LOCKED("Account is temporarily locked"),
    ACCOUNT_NOT_BANNED("Account is not banned"),
    TOKEN_NOT_BELONG_TO_ACCOUNT("Token does not belong to this account"),
    TOKEN_EXPIRED("Token is expired"),
    TOKEN_ALREADY_USED("Token is already used"),
    INVALID_TOKEN("Invalid or incorrect token/OTP"),
    INVALID_TOKEN_STATE("Token is not in the expected state for this operation"),
    CHANGE_REQUEST_NOT_FOUND("Change contact request not found or expired"),
    CONTACT_NOT_FOUND("Contact not found for the requested verification method"),
    ROLE_NAME_EMPTY("RoleName cannot be null or empty"),
    ROLE_NAME_INVALID_LENGTH("RoleName length must be between 3 and 100 characters"),
    BAN_REASON_EMPTY("Ban reason cannot be null or empty"),
    BAN_REASON_EXCEEDS_LENGTH("Ban reason cannot exceed 500 characters"),
    EMAIL_EMPTY("Email cannot be null or empty"),
    EMAIL_INVALID("Invalid email format"),
    USERNAME_EMPTY("Username cannot be null or empty"),
    USERNAME_INVALID_LENGTH("Username length must be between 3 and 100 characters"),
    PASSWORD_HASH_EMPTY("Password hash cannot be null or empty"),
    INVALID_PASSWORD("Invalid password"),
    ACCOUNT_NOT_VERIFIED("Account is not verified"),
    NOT_ENOUGH_PERMISSION("Not enough permission"),
    /**
     * <h1>Payment</h1>
     */
    INVALID_GATEWAY_RESPONSE("Invalid gateway response"),
    PAYMENT_AMOUNT_MISMATCH("Payment amount mismatch"),

    /**
     * <h1>Profile</h1>
     */
    PROFILE_NOT_FOUND("User profile not found"),
    DISPLAY_NAME_ALREADY_EXISTS("Display name already exists"),
    PHONE_NUMBER_EMPTY("Phone number cannot be null or empty"),
    PHONE_NUMBER_INVALID("Invalid phone number format"),
    PHONE_NUMBER_ALREADY_EXISTS("Phone number already exists"),
    EMAIL_CHANGE_NOT_SUPPORTED("Changing email address is not supported");

    private final String description;

    ErrorCode(String description) {
        this.description = description;
    }

}
