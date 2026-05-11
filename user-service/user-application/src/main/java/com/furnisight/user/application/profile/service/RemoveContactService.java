package com.furnisight.user.application.profile.service;

import com.furnisight.user.application.profile.dto.RemoveContactCommand;
import com.furnisight.user.application.profile.port.in.usecase.RemoveContactUseCase;
import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.entities.profile.UserProfile;
import com.furnisight.user.domain.exceptions.identity.ErrorCode;
import com.furnisight.user.domain.exceptions.identity.NotFoundException;
import com.furnisight.user.domain.exceptions.identity.ValidationException;
import com.furnisight.user.domain.repository.identity.AccountRepository;
import com.furnisight.user.domain.repository.profile.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RemoveContactService implements RemoveContactUseCase {

    private final AccountRepository accountRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    @Transactional
    public Void execute(RemoveContactCommand command) {
//        Account account = loadAccount(command.accountId());
//        UserProfile profile = loadProfile(command.accountId());
//
//        if ("EMAIL".equalsIgnoreCase(command.type())) {
//            account.changeEmail(null);
//            profile.setEmail(null);
//        } else if ("PHONE".equalsIgnoreCase(command.type())) {
//            profile.setPhoneNumber(null);
//        } else {
//            throw new ValidationException(ErrorCode.INVALID_INPUT);
//        }
//
//        accountRepository.save(account);
//        userProfileRepository.save(profile);

        return null;
    }

    private Account loadAccount(UUID accountId) {
        return accountRepository.findById(accountId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    private UserProfile loadProfile(UUID accountId) {
        return userProfileRepository.findByAccountId(accountId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.PROFILE_NOT_FOUND));
    }
}
