package com.furnisight.user.domain.services.identity.generator;

public interface OtpHasher {
    String hash(String rawOtp);
}
