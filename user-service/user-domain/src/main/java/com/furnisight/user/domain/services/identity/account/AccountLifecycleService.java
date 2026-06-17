package com.furnisight.user.domain.services.identity.account;

import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.entities.identity.SocialAccount;
import com.furnisight.user.domain.enums.identity.SocialProvider;
import com.furnisight.user.domain.exceptions.identity.AlreadyExistsException;
import com.furnisight.user.domain.exceptions.identity.ErrorCode;
import com.furnisight.user.domain.exceptions.identity.NotFoundException;
import com.furnisight.user.domain.repository.identity.AccountRepository;
import com.furnisight.user.domain.repository.identity.SocialAccountRepository;
import com.furnisight.user.domain.services.identity.policy.PasswordPolicy;
import com.furnisight.user.domain.events.identity.AccountCreatedEvent;
import com.furnisight.user.domain.events.identity.AccountDeletedEvent;
import com.furnisight.user.domain.events.identity.SocialAccountCreatedEvent;
import com.furnisight.user.domain.valueobjects.identity.Email;
import com.furnisight.user.domain.valueobjects.identity.Password;
import com.furnisight.user.domain.valueobjects.identity.Username;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountLifecycleService {
    private final AccountRepository accountRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final PasswordPolicy passwordPolicy;
    private final PasswordHasher passwordHasher;
    private final VerificationService verificationService;

    public Account register(Email email, String password) {
        Username username = Username.fromEmail(email.getValue());
        if (accountRepository.existsAccount(username, email)) {
            throw new AlreadyExistsException(ErrorCode.ACCOUNT_ALREADY_EXISTS);
        }
        passwordPolicy.validate(password);
        Password hashedPassword = new Password(passwordHasher.hash(password));
        Account account = new Account(username, email, hashedPassword);
        account.registerEvent(new AccountCreatedEvent(account.getId(), email.getValue(), LocalDateTime.now()));
        account = accountRepository.save(account);
        verificationService.requestVerification(account, account.getEmail().getValue());
        return account;
    }

    public Account provision(Email email, String password) {
        Username username = Username.fromEmail(email.getValue());
        if (accountRepository.existsAccount(username, email)) {
            throw new AlreadyExistsException(ErrorCode.ACCOUNT_ALREADY_EXISTS);
        }
        passwordPolicy.validate(password);
        Password hashedPassword = new Password(passwordHasher.hash(password));
        Account account = new Account(username, email, hashedPassword);
        account.activate();
        account.registerEvent(new AccountCreatedEvent(account.getId(), email.getValue(), LocalDateTime.now()));
        return accountRepository.save(account);
    }

    public Account LoginWithSocialAccount(
            Optional<SocialAccount> existingSocialAccount,
            SocialProvider provider,
            String providerUserId,
            Email email) {
        if (existingSocialAccount.isPresent()) {
            return accountRepository.findById(existingSocialAccount.get().getAccountId())
                    .orElseThrow(() -> new NotFoundException(ErrorCode.ACCOUNT_NOT_FOUND));
        }
        String rawUsername = (email != null) ? email.getValue() : UUID.randomUUID().toString();
        Username username = new Username(rawUsername);

        String randomPassword = UUID.randomUUID().toString();
        Password hashedPassword = new Password(passwordHasher.hash(randomPassword));

        Account account = new Account(username, email, hashedPassword);
        account.activate();
        account.registerEvent(new AccountCreatedEvent(
                account.getId(),
                email != null ? email.getValue() : null,
                LocalDateTime.now()));

        if (email != null) {
            account.registerEvent(new SocialAccountCreatedEvent(
                    account.getId(),
                    email.getValue(),
                    randomPassword,
                    LocalDateTime.now()));
        }

        accountRepository.save(account);
        SocialAccount socialAccount = new SocialAccount(account.getId(), provider, providerUserId, email);
        socialAccountRepository.save(socialAccount);
        return account;
    }

    public void deleteAccount(Account account) {
        account.registerEvent(new AccountDeletedEvent(
                account.getId(),
                account.getEmail() != null ? account.getEmail().getValue() : null,
                LocalDateTime.now()));
        accountRepository.save(account);
        accountRepository.delete(account);
    }
}
