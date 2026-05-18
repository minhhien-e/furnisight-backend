package com.furnisight.review.core.model.valueobject;

import com.furnisight.review.core.exception.ReviewDomainException;
import com.furnisight.review.core.exception.enums.ErrorCode;
import jakarta.persistence.Embeddable;
import java.util.Map;

@Embeddable
public record ClientIp(String value) {

    public ClientIp {
        if (value == null || value.isBlank()) {
            throw new ReviewDomainException(ErrorCode.INVALID_INPUT_DATA,
                Map.of("field", "ClientIp", "reason", "Cannot be empty"));
        }
        if (!isValidIp(value)) {
            throw new ReviewDomainException(ErrorCode.INVALID_IP_ADDRESS,
                Map.of("invalidValue", value));
        }
    }

    private boolean isValidIp(String ip) {
        return ip.matches("(\\d{1,3}\\.){3}\\d{1,3}");
    }
}
