package com.furnisight.review.core.security;

import java.util.UUID;

public interface CurrentUserProvider {
    UUID getCurrentUserId();
}
