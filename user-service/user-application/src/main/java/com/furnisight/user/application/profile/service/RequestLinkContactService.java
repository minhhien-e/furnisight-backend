package com.furnisight.user.application.profile.service;

import com.furnisight.user.application.profile.dto.RequestLinkContactCommand;
import com.furnisight.user.application.profile.port.in.usecase.RequestLinkContactUseCase;
import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.enums.identity.VerificationType;
import com.furnisight.user.domain.exceptions.identity.ErrorCode;
import com.furnisight.user.domain.exceptions.identity.NotFoundException;
import com.furnisight.user.domain.exceptions.identity.ValidationException;
import com.furnisight.user.domain.repository.identity.AccountRepository;
import com.furnisight.user.domain.services.profile.UserProfileLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RequestLinkContactService implements RequestLinkContactUseCase {

    private final AccountRepository accountRepository;
    private final UserProfileLifecycleService contactChangeService;

    @Override
    @Transactional
    public Void execute(RequestLinkContactCommand command) {
        VerificationType type = resolveType(command.type());
        Account account = loadAccount(command.accountId());
        
        contactChangeService.initiateContactLink(account, type, command.newContact());
        return null;
    }

    private Account loadAccount(UUID accountId) {
        return accountRepository.findById(accountId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    private VerificationType resolveType(VerificationType type) {
        if (type == VerificationType.EMAIL_LINK || type == VerificationType.PHONE_LINK) {
            return type;
        }
        throw new ValidationException(ErrorCode.INVALID_TOKEN_STATE);
    }
}
