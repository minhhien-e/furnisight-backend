package com.furnisight.user.application.account.service;

import com.furnisight.user.application.account.dto.VerifyAccountCommand;
import com.furnisight.user.application.account.port.in.usecase.VerifyAccountUseCase;
import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.enums.identity.VerificationType;
import com.furnisight.user.domain.exceptions.DomainException;
import com.furnisight.user.domain.exceptions.identity.ErrorCode;
import com.furnisight.user.domain.exceptions.identity.NotFoundException;
import com.furnisight.user.domain.repository.identity.AccountRepository;
import com.furnisight.user.domain.repository.identity.OtpVerificationRepository;
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
    private final OtpVerificationRepository otpVerificationRepository;

    @Override
    @Transactional
    public Void execute(VerifyAccountCommand command) {
        String otpHash = otpVerificationRepository.findHashByEmailAndType(command.email(), VerificationType.ACCOUNT_VERIFICATION)
                .orElseThrow(() -> new DomainException(ErrorCode.TOKEN_EXPIRED));

        Account account = accountRepository.findByEmail(new Email(command.email()))
                .orElseThrow(() -> new NotFoundException(ErrorCode.ACCOUNT_NOT_FOUND));

        verificationService.verifyAccount(account, otpHash, command.otpCode());
        return null;
    }
}
