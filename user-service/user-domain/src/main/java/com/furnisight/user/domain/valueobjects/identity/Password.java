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
public class Password extends ValueObject {

    @Column(name = "password_hash")
    private String hash;

    public Password(String hash) {
        if (hash != null) {
            if (hash.trim().isEmpty()) {
                throw new ValidationException(ErrorCode.PASSWORD_HASH_EMPTY);
            }
        }
        this.hash = hash;
    }

    @Override
    protected List<Object> getEqualityComponents() {
        return Collections.singletonList(hash);
    }
}
