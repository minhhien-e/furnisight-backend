package com.furnisight.user.infrastructure.database.repository.jpa.identity;

import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.enums.identity.AccountStatus;
import com.furnisight.user.domain.valueobjects.identity.Email;
import com.furnisight.user.domain.valueobjects.identity.Username;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;
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
            WHERE LOWER(a.email.value) = LOWER(:identifier)
               OR a.username.value = :identifier
        """)
    Optional<Account> findByIdentifier(String identifier);

    @Query("SELECT a FROM Account a WHERE " +
           "(:query IS NULL OR :query = '' OR LOWER(a.username.value) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(a.email.value) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:status IS NULL OR a.status = :status)")
    org.springframework.data.domain.Page<Account> searchAccounts(@Param("query") String query, @Param("status") AccountStatus status, org.springframework.data.domain.Pageable pageable);

    @Query("""
            SELECT a FROM Account a
            WHERE (:query IS NULL OR :query = '' OR LOWER(a.username.value) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(a.email.value) LIKE LOWER(CONCAT('%', :query, '%')))
              AND (:status IS NULL OR a.status = :status)
              AND NOT EXISTS (
                  SELECT ar.id FROM AccountRole ar, Role r
                  WHERE ar.accountId = a.id AND r.id = ar.roleId
                    AND UPPER(r.name.value) IN :adminRoles
              )
            """)
    org.springframework.data.domain.Page<Account> searchCustomerAccounts(
            @Param("query") String query,
            @Param("status") AccountStatus status,
            @Param("adminRoles") java.util.Collection<String> adminRoles,
            org.springframework.data.domain.Pageable pageable);

    @Query("""
            SELECT DISTINCT a FROM Account a, AccountRole ar, Role r
            WHERE ar.accountId = a.id AND r.id = ar.roleId
              AND UPPER(r.name.value) IN :adminRoles
              AND (:query IS NULL OR :query = '' OR LOWER(a.username.value) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(a.email.value) LIKE LOWER(CONCAT('%', :query, '%')))
              AND (:status IS NULL OR a.status = :status)
            """)
    org.springframework.data.domain.Page<Account> searchAdministrativeAccounts(
            @Param("query") String query,
            @Param("status") AccountStatus status,
            @Param("adminRoles") java.util.Collection<String> adminRoles,
            org.springframework.data.domain.Pageable pageable);

    List<Account> findAllByStatus(AccountStatus status);

    List<Account> findAllByStatusAndCreatedAtAfter(AccountStatus status, LocalDateTime createdAfter);

    List<Account> findAllByStatusAndUpdatedAtBefore(AccountStatus status, LocalDateTime updatedBefore);

    long countByStatus(AccountStatus status);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
