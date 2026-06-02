package com.furnisight.user.infrastructure.database.repository.jpa.identity;

import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.enums.identity.AccountStatus;
import com.furnisight.user.domain.valueobjects.identity.Email;
import com.furnisight.user.domain.valueobjects.identity.Username;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

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

    @Query("SELECT a FROM Account a WHERE " +
           "(:query IS NULL OR :query = '' OR LOWER(a.username.value) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(a.email.value) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:status IS NULL OR a.status = :status)")
    org.springframework.data.domain.Page<Account> searchAccounts(@Param("query") String query, @Param("status") AccountStatus status, org.springframework.data.domain.Pageable pageable);

    long countByStatus(AccountStatus status);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
