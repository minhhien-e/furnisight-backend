package com.furnisight.catalog.application.review.port.in.security;

import java.util.UUID;

public interface CurrentUserProvider {
    UUID getCurrentUserId();
}
