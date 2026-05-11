package com.furnisight.user.application.profile.service;

import com.furnisight.user.application.profile.dto.RequestNewContactCommand;
import com.furnisight.user.application.profile.port.in.usecase.RequestNewContactUseCase;
import com.furnisight.user.domain.entities.identity.VerificationRequest;
import com.furnisight.user.domain.enums.identity.VerificationType;
import com.furnisight.user.domain.exceptions.identity.ErrorCode;
import com.furnisight.user.domain.exceptions.identity.NotFoundException;
import com.furnisight.user.domain.exceptions.identity.ValidationException;
import com.furnisight.user.domain.repository.identity.VerificationRequestRepository;
import com.furnisight.user.domain.services.profile.UserProfileLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RequestNewContactService implements RequestNewContactUseCase {

    private final VerificationRequestRepository verificationRequestRepository;
    private final UserProfileLifecycleService contactChangeService;

    @Override
    @Transactional
    public Void execute(RequestNewContactCommand command) {
        VerificationType type = resolveType(command.type());
        VerificationRequest request = loadPendingRequest(command.accountId(), type);

        contactChangeService.submitNewContact(request, command.newContact());
        return null;
    }

    private VerificationRequest loadPendingRequest(UUID accountId, VerificationType type) {
        return verificationRequestRepository.findActiveByAccountIdAndType(accountId, type)
            .orElseThrow(() -> new NotFoundException(ErrorCode.CHANGE_REQUEST_NOT_FOUND));
    }

    private VerificationType resolveType(VerificationType type) {
        if (type == VerificationType.EMAIL_CHANGE || type == VerificationType.PHONE_CHANGE) {
            return type;
        }
        throw new ValidationException(ErrorCode.INVALID_TOKEN_STATE);
    }
}
