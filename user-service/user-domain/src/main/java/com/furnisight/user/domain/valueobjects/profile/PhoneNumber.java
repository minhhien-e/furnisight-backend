package com.furnisight.user.domain.valueobjects.profile;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import com.furnisight.user.domain.exceptions.identity.ErrorCode;
import com.furnisight.user.domain.exceptions.identity.ValidationException;
import com.furnisight.user.domain.seedwork.ValueObject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PhoneNumber extends ValueObject {
    private static final Pattern PHONENE_PATTERN = Pattern.compile("^(\\+84|0)(3|5|7|8|9)[0-9]{8}$");

    @Column(name = "phone_number", length = 15)
    private String value;

    public PhoneNumber(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(ErrorCode.PHONE_NUMBER_EMPTY);
        }

        if (!PHONENE_PATTERN.matcher(value.trim()).matches()) {
            throw new ValidationException(ErrorCode.PHONE_NUMBER_INVALID);
        }
        this.value = value.trim();
    }

    @Override
    protected List<Object> getEqualityComponents() {
        return Collections.singletonList(value);
    }
}
