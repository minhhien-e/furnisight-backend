package com.furnisight.user.application.profile.service;

import com.furnisight.user.application.profile.dto.RequestContactChangeCommand;
import com.furnisight.user.application.profile.port.in.usecase.RequestContactChangeUseCase;
import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.entities.profile.UserProfile;
import com.furnisight.user.domain.enums.identity.VerificationType;
import com.furnisight.user.domain.exceptions.identity.ErrorCode;
import com.furnisight.user.domain.exceptions.identity.NotFoundException;
import com.furnisight.user.domain.exceptions.identity.ValidationException;
import com.furnisight.user.domain.repository.identity.AccountRepository;
import com.furnisight.user.domain.repository.profile.UserProfileRepository;
import com.furnisight.user.domain.services.profile.UserProfileLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RequestContactChangeService implements RequestContactChangeUseCase {

    private final AccountRepository accountRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserProfileLifecycleService contactChangeService;

    @Override
    @Transactional
    public Void execute(RequestContactChangeCommand command) {
        VerificationType type = resolveType(command.type());
        Account account = loadAccount(command.accountId());
        UserProfile profile = loadProfile(command.accountId());
        contactChangeService.initiateContactChange(account, profile, type, command.verifyBy());
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

    private VerificationType resolveType(VerificationType type) {
        if (type == VerificationType.EMAIL_CHANGE || type == VerificationType.PHONE_CHANGE) {
            return type;
        }
        throw new ValidationException(ErrorCode.INVALID_TOKEN_STATE);
    }
}
