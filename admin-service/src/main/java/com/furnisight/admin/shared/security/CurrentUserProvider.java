package com.furnisight.admin.shared.security;

import java.util.UUID;

public interface CurrentUserProvider {
    UUID getCurrentUserId();
}
