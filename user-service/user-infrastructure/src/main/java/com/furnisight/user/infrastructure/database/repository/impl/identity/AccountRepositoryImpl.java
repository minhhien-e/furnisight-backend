package com.furnisight.user.infrastructure.database.repository.impl.identity;

import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.repository.identity.AccountRepository;
import com.furnisight.user.domain.valueobjects.identity.Email;
import com.furnisight.user.domain.valueobjects.identity.Username;
import com.furnisight.user.infrastructure.database.repository.jpa.identity.AccountJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AccountRepositoryImpl implements AccountRepository {
    private final AccountJpaRepository accountJpaRepository;

    @Override
    public Account save(Account account) {
        return accountJpaRepository.save(account);
    }

    @Override
    public void delete(Account account) {
        accountJpaRepository.delete(account);
    }

    @Override
    public Optional<Account> findById(UUID id) {
        return accountJpaRepository.findById(id);
    }

    @Override
    public Optional<Account> findByUsername(Username username) {
        return accountJpaRepository.findByUsername(username);
    }

    @Override
    public Optional<Account> findByEmail(Email email) {
        return accountJpaRepository.findByEmail(email);

    }

    @Override
    public boolean existsAccount(Username username, Email email) {
        boolean existsByUsername = accountJpaRepository.existsByUsername(username);
        boolean existsByEmail = accountJpaRepository.existsByEmail(email);
        return existsByUsername || existsByEmail;
    }

    @Override
    public Optional<Account> findByCredential(String identifier) {
        return accountJpaRepository.findByIdentifier(identifier);
    }
}
