package com.furnisight.user.application.account.service;

import com.furnisight.user.application.account.dto.ResetPasswordCommand;
import com.furnisight.user.application.account.port.in.usecase.ResetPasswordUseCase;
import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.exceptions.DomainException;
import com.furnisight.user.domain.exceptions.identity.ErrorCode;
import com.furnisight.user.domain.exceptions.identity.NotFoundException;
import com.furnisight.user.domain.entities.identity.VerificationRequest;
import com.furnisight.user.domain.enums.identity.VerificationType;
import com.furnisight.user.domain.repository.identity.AccountRepository;
import com.furnisight.user.domain.repository.identity.VerificationRequestRepository;
import com.furnisight.user.domain.services.identity.account.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResetPasswordService implements ResetPasswordUseCase {

    private final PasswordService passwordService;
    private final AccountRepository accountRepository;
    private final VerificationRequestRepository verificationRequestRepository;

    @Override
    @Transactional
    public Void execute(ResetPasswordCommand command) {
        VerificationRequest request = verificationRequestRepository.findByOtpCodeAndType(command.token(), VerificationType.PASSWORD_RESET)
                .orElseThrow(() -> new DomainException(ErrorCode.TOKEN_EXPIRED));

        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.ACCOUNT_NOT_FOUND));

        passwordService.resetPassword(account, request, command.token(), command.newPassword());
        verificationRequestRepository.save(request); // persist COMPLETED step
        return null;
    }
}
