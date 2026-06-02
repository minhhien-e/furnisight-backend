package com.furnisight.admin.security;

import java.util.UUID;

public interface CurrentUserProvider {
    UUID getCurrentUserId();
}
