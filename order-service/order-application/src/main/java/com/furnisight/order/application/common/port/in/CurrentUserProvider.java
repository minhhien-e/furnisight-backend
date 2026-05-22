package com.furnisight.order.application.common.port.in;

import java.util.UUID;

public interface CurrentUserProvider {
    UUID getCurrentUserId();
}
