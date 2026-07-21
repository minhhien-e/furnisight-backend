package com.furnisight.user.domain.repository.identity;

import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.valueobjects.identity.Email;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository{
    Account save(Account account);
    void delete(Account account);
    Optional<Account> findById(UUID id);
    Optional<Account> findByEmail(Email email);
    Optional<Email> findEmailById(UUID id);
    boolean existsAccount(Email email);
    Optional<Account> findByCredential(String identifier);
    void updateLoginStatus(Account account);
}
