package com.furnisight.user.domain.entities.identity;

import com.furnisight.user.domain.seedwork.AggregateRoot;
import com.furnisight.user.domain.valueobjects.identity.AccessToken;
import com.furnisight.user.domain.valueobjects.identity.RefreshToken;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "account_tokens")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountToken extends AggregateRoot {
    @Id
    private UUID id;
    private UUID accountId;
    private AccessToken accessToken;
    private RefreshToken refreshToken;

    public AccountToken(UUID accountId, AccessToken accessToken, RefreshToken refreshToken) {
        this.id = UUID.randomUUID();
        this.accountId = accountId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
    public void revoke() {
        accessToken.revoke();
        refreshToken.revoke();
    }
    public boolean refreshTokenIsExpired() {
        return refreshToken.isExpired();
    }
}
