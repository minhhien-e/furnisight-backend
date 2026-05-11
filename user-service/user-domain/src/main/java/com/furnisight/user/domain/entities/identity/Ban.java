package com.furnisight.user.domain.entities.identity;

import com.furnisight.user.domain.seedwork.AggregateRoot;
import com.furnisight.user.domain.valueobjects.identity.BanReason;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bans")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ban extends AggregateRoot {

    @Id
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Embedded
    private BanReason reason;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    public Ban(UUID accountId, BanReason reason, LocalDateTime expiresAt) {
        this.id = UUID.randomUUID();
        this.accountId = accountId;
        this.reason = reason;
        this.expiresAt = expiresAt;
        this.isActive = true;
    }

    public boolean isPermanent() {
        return expiresAt == null;
    }

    public void liftBan() {
        this.isActive = false;
    }
}
