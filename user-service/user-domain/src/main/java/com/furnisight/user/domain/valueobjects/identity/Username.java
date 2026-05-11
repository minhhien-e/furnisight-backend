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
public class Username extends ValueObject {

    @Column(name = "username", unique = true, length = 100)
    private String value;

    public Username(String value) {
        if (value != null) {
            if (value.trim().isEmpty()) {
                throw new ValidationException(ErrorCode.USERNAME_EMPTY);
            }
            if (value.length() < 3 || value.length() > 100) {
                throw new ValidationException(ErrorCode.USERNAME_INVALID_LENGTH);
            }
        }
        this.value = value;
    }

    @Override
    protected List<Object> getEqualityComponents() {
        return Collections.singletonList(value);
    }
}
