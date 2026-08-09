package com.furnisight.user.domain.entities.profile;

import com.furnisight.user.domain.enums.profile.Gender;
import com.furnisight.user.domain.seedwork.AggregateRoot;
import com.furnisight.user.domain.valueobjects.identity.Email;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfile extends AggregateRoot {

    @Id
    private UUID id;

    @Column(name = "account_id", nullable = false, unique = true)
    private UUID accountId;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "avatar_media_id")
    private UUID avatarMediaId;

    @Column(name = "avatar_url", length = 2048)
    private String avatarUrl;

    @Column(name = "bio", length = 500)
    private String bio;

    @Column(name = "email", length = 255)
    private Email email;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;

    public UserProfile(UUID accountId, String fullName, Email email) {
        this.id = UUID.randomUUID();
        this.accountId = accountId;
        this.fullName = fullName;
        this.email = email;
    }

    public UserProfile(UUID accountId, String fullName, Email email, String avatarUrl) {
        this.id = UUID.randomUUID();
        this.accountId = accountId;
        this.fullName = fullName;
        this.email = email;
        this.avatarUrl = avatarUrl;
    }

    public void updateProfile(String displayName, String fullName,
                              UUID avatarMediaId, String bio,
                              LocalDate dateOfBirth, Gender gender) {
        this.displayName = displayName;
        this.fullName = fullName;
        this.avatarMediaId = avatarMediaId;
        this.bio = bio;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
    }

}
