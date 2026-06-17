package com.furnisight.order.application.user.port.out;

import java.util.UUID;

public interface UserEmailPort {
    String getEmailByUserId(UUID userId);
}
