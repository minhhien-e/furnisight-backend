package com.furnisight.user.application.account.service;

import com.furnisight.user.application.account.dto.LoginWithSocialAccountCommand;
import com.furnisight.user.application.account.port.in.usecase.LoginWithSocialAccountUseCase;
import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.entities.identity.AccountToken;
import com.furnisight.user.domain.entities.identity.SocialAccount;
import com.furnisight.user.domain.repository.identity.SocialAccountRepository;
import com.furnisight.user.domain.services.identity.account.AccountLifecycleService;
import com.furnisight.user.domain.services.identity.token.TokenLifeCycleService;
import com.furnisight.user.domain.services.profile.UserProfileLifecycleService;
import com.furnisight.user.domain.valueobjects.identity.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginWithSocialAccountService implements LoginWithSocialAccountUseCase {

        private final SocialAccountRepository socialAccountRepository;
        private final AccountLifecycleService accountLifecycleService;
        private final UserProfileLifecycleService userProfileLifecycleService;
        private final TokenLifeCycleService tokenLifeCycleService;

        @Override
        @Transactional
        public AccountToken execute(LoginWithSocialAccountCommand command) {
                Optional<SocialAccount> existingSocialAccount = socialAccountRepository
                                .findByProviderUserId(command.providerUserId(), command.provider());

                Email emailVO = command.email() != null ? new Email(command.email()) : null;

                Account account = accountLifecycleService.LoginWithSocialAccount(
                                existingSocialAccount,
                                command.provider(),
                                command.providerUserId(),
                                emailVO);

                if (existingSocialAccount.isEmpty()) {
                        String resolvedFirstName = command.firstName() != null
                                        ? command.firstName()
                                        : (command.fullName() != null ? command.fullName().split(" ")[0] : null);
                        String resolvedLastName = command.lastName() != null
                                        ? command.lastName()
                                        : (command.fullName() != null && command.fullName().contains(" ")
                                                        ? command.fullName()
                                                                        .substring(command.fullName().indexOf(' ') + 1)
                                                        : null);

                        userProfileLifecycleService.createProfile(
                                        account.getId(),
                                        resolvedFirstName,
                                        resolvedLastName,
                                        command.email(),
                                        command.avatarUrl());
                }

                return tokenLifeCycleService.generateAccountToken(account);
        }
}
