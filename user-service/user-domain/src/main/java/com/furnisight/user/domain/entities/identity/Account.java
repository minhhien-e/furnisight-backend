package com.furnisight.user.domain.entities.identity;

import com.furnisight.user.domain.enums.identity.AccountStatus;
import com.furnisight.user.domain.seedwork.AggregateRoot;
import com.furnisight.user.domain.valueobjects.identity.Email;
import com.furnisight.user.domain.valueobjects.identity.Password;
import com.furnisight.user.domain.valueobjects.identity.Username;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
@Setter
public class Account extends AggregateRoot {

    @Id
    private UUID id;

    @Embedded
    private Username username;

    @Embedded
    private Email email;

    @Embedded
    private Password password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status = AccountStatus.UNVERIFIED;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts = 0;

    @Column(name = "lockout_end")
    private LocalDateTime lockoutEnd;

    public Account(Username username, Email email, Password password) {
        this.id = UUID.randomUUID();
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public Account() {
        this.id = UUID.randomUUID();
    }

    public void ban() {
        this.status = AccountStatus.BANNED;
    }

    public void activate() {
        this.status = AccountStatus.ACTIVE;
    }

    public boolean isBanned() {
        return this.status == AccountStatus.BANNED;
    }

    public boolean isLocked() {
        if (this.status == AccountStatus.LOCKED) {
            if (this.lockoutEnd != null && LocalDateTime.now().isAfter(this.lockoutEnd)) {
                this.status = AccountStatus.ACTIVE; // Auto unlock
                this.lockoutEnd = null;
                this.failedLoginAttempts = 0;
                return false;
            }
            return true;
        }
        return false;
    }

    public boolean isVerified() {
        return this.status == AccountStatus.ACTIVE;
    }

    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
    }

    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.lockoutEnd = null;
    }

    public void lockTemporarily(LocalDateTime lockoutEnd) {
        this.status = AccountStatus.LOCKED;
        this.lockoutEnd = lockoutEnd;
    }

    public void changeEmail(Email newEmail) {
        this.email = newEmail;
    }
}
