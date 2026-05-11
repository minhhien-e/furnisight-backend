package com.furnisight.user.domain.entities.identity;

import com.furnisight.user.domain.enums.identity.SocialProvider;
import com.furnisight.user.domain.seedwork.BaseEntity;
import com.furnisight.user.domain.valueobjects.identity.Email;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "social_accounts",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"provider", "provider_user_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialAccount extends BaseEntity {

    @Id
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SocialProvider provider;

    @Column(name = "provider_user_id", nullable = false, length = 255)
    private String providerUserId;

    @Column(name = "email", length = 255)
    private Email email;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    public SocialAccount(UUID accountId, SocialProvider provider, String providerUserId, Email email) {
        this.id = UUID.randomUUID();
        this.provider = provider;
        this.accountId = accountId;
        this.providerUserId = providerUserId;
        this.email = email;
    }
}
