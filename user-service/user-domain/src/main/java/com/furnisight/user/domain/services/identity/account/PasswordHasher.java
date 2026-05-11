package com.furnisight.user.domain.services.identity.account;

public interface PasswordHasher {
    String hash(String password);
    boolean verify(String password, String hashedPassword);
}
