package com.furnisight.user.domain.valueobjects.identity;

import com.furnisight.user.domain.seedwork.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken extends ValueObject {
    @Column(name = "refresh_token", nullable = false)
    private String value;
    @Column(name = "refresh_token_expiration", nullable = false)
    private LocalDateTime expirationDate;
    @Column(name = "refresh_token_revoked", nullable = false)
    private boolean isRevoked;

    public RefreshToken(String value, LocalDateTime expirationDate, boolean isRevoked) {
        this.value = value;
        this.expirationDate = expirationDate;
        this.isRevoked = isRevoked || isExpired();
    }
    public RefreshToken(String value) {
        this(value, LocalDateTime.now(), false);
    }

    public void revoke() {
        this.isRevoked = true;
    }

    public boolean isExpired() {
        return isRevoked || LocalDateTime.now().isAfter(expirationDate);
    }

    @Override
    protected List<Object> getEqualityComponents() {
        return Collections.singletonList(value);
    }

}
