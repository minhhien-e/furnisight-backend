package com.furnisight.review.application.review.port.in.security;

import java.util.UUID;

public interface CurrentUserProvider {
    UUID getCurrentUserId();
}

