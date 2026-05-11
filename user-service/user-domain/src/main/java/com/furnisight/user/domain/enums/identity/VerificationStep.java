package com.furnisight.user.domain.enums.identity;

public enum VerificationStep {
    STEP_1_PENDING,   // waiting for OTP at target/current contact
    STEP_2_PENDING,   // waiting for OTP at new contact (for 2-step flows)
    COMPLETED         // fully verified/completed
}
