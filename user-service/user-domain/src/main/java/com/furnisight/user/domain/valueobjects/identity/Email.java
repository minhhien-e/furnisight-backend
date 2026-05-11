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
import java.util.regex.Pattern;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Email extends ValueObject {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    @Column(name = "email", unique = true, length = 255)
    private String value;

    public Email(String value) {
        if (value != null) {
            if (value.trim().isEmpty()) {
                throw new ValidationException(ErrorCode.EMAIL_EMPTY);
            }
            if (!EMAIL_PATTERN.matcher(value).matches()) {
                throw new ValidationException(ErrorCode.EMAIL_INVALID);
            }
        }
        this.value = value;
    }

    @Override
    protected List<Object> getEqualityComponents() {
        return Collections.singletonList(value);
    }
}
