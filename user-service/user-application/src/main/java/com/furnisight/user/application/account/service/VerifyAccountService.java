package com.furnisight.user.application.account.service;

import com.furnisight.user.application.account.dto.VerifyAccountCommand;
import com.furnisight.user.application.account.port.in.usecase.VerifyAccountUseCase;
import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.entities.identity.VerificationRequest;
import com.furnisight.user.domain.enums.identity.VerificationType;
import com.furnisight.user.domain.exceptions.DomainException;
import com.furnisight.user.domain.exceptions.identity.ErrorCode;
import com.furnisight.user.domain.exceptions.identity.NotFoundException;
import com.furnisight.user.domain.repository.identity.AccountRepository;
import com.furnisight.user.domain.repository.identity.VerificationRequestRepository;
import com.furnisight.user.domain.services.identity.account.VerificationService;
import com.furnisight.user.domain.valueobjects.identity.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VerifyAccountService implements VerifyAccountUseCase {

    private final VerificationService verificationService;
    private final AccountRepository accountRepository;
    private final VerificationRequestRepository verificationRequestRepository;

    @Override
    @Transactional
    public Void execute(VerifyAccountCommand command) {
        VerificationRequest request = verificationRequestRepository.findByOtpCodeAndType(command.otpCode(), VerificationType.ACCOUNT_VERIFICATION)
                .orElseThrow(() -> new DomainException(ErrorCode.TOKEN_EXPIRED));

        Account account = accountRepository.findByEmail(new Email(request.getTargetContact()))
                .orElseThrow(() -> new NotFoundException(ErrorCode.ACCOUNT_NOT_FOUND));

        verificationService.verifyAccount(account, request, command.otpCode());
        return null;
    }
}
