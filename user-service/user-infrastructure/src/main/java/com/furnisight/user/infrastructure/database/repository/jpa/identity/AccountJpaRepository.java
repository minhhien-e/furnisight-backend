package com.furnisight.user.infrastructure.database.repository.jpa.identity;

import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.valueobjects.identity.Email;
import com.furnisight.user.domain.valueobjects.identity.Username;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AccountJpaRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByUsername(Username username);

    Optional<Account> findByEmail(Email email);

    boolean existsByUsername(Username username);

    @Query("SELECT COUNT(a) > 0 FROM Account a WHERE a.email = :email AND a.status = 'ACTIVE'")
    boolean existsByEmail(@Param("email") Email email);

    @Query("""
            SELECT a FROM Account a
            WHERE a.email.value = :identifier
               OR a.username.value = :identifier
        """)
    Optional<Account> findByIdentifier(String identifier);
}
