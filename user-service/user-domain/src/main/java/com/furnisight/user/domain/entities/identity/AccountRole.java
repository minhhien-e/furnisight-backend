package com.furnisight.user.domain.entities.identity;

import com.furnisight.user.domain.seedwork.AggregateRoot;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "account_roles")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountRole extends AggregateRoot {

    @Id
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    public AccountRole(UUID accountId, UUID roleId) {
        this.id = UUID.randomUUID();
        this.accountId = accountId;
        this.roleId = roleId;
    }
}
