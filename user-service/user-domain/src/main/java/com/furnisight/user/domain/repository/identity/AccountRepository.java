package com.furnisight.user.domain.repository.identity;

import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.valueobjects.identity.Email;
import com.furnisight.user.domain.valueobjects.identity.Username;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository{
    Account save(Account account);
    void delete(Account account);
    Optional<Account> findById(UUID id);
    Optional<Account> findByUsername(Username username);
    Optional<Account> findByEmail(Email email);
    boolean existsAccount(Username username, Email email);
    Optional<Account> findByCredential(String identifier);
}
