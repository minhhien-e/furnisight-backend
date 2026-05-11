package com.furnisight.user.domain.valueobjects.identity;

import com.furnisight.user.domain.seedwork.ValueObject;
import com.furnisight.user.domain.exceptions.identity.ValidationException;
import com.furnisight.user.domain.exceptions.identity.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BanReason extends ValueObject {

    @Column(name = "reason", nullable = false, length = 500)
    private String value;

    public BanReason(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(ErrorCode.BAN_REASON_EMPTY);
        }
        if (value.length() > 500) {
            throw new ValidationException(ErrorCode.BAN_REASON_EXCEEDS_LENGTH);
        }
        this.value = value;
    }

    @Override
    protected List<Object> getEqualityComponents() {
        return Collections.singletonList(value);
    }
}
