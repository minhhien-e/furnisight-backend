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
public class RoleName extends ValueObject {
    @Column(unique = true, nullable = false, length = 50, name = "name")
    private String value;

    public RoleName(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(ErrorCode.ROLE_NAME_EMPTY);
        }
        if (value.length() < 3 || value.length() > 50) {
            throw new ValidationException(ErrorCode.ROLE_NAME_INVALID_LENGTH);
        }
        this.value = value;
    }

    @Override
    protected List<Object> getEqualityComponents() {
        return Collections.singletonList(value);
    }
}
