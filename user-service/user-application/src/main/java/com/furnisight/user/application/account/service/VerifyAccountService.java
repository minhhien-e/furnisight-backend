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
import com.furnisight.user.domain.services.identity.generator.OtpHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VerifyAccountService implements VerifyAccountUseCase {

    private final VerificationService verificationService;
    private final AccountRepository accountRepository;
    private final OtpVerificationRepository otpVerificationRepository;
    private final OtpHasher otpHasher;

    @Override
    @Transactional
    public Void execute(VerifyAccountCommand command) {
        String hash = otpHasher.hash(command.otpCode());
        String email = otpVerificationRepository.findEmailByHashAndType(hash, VerificationType.ACCOUNT_VERIFICATION)
                .orElseThrow(() -> new DomainException(ErrorCode.TOKEN_EXPIRED));

        Account account = accountRepository.findByEmail(new Email(email))
                .orElseThrow(() -> new NotFoundException(ErrorCode.ACCOUNT_NOT_FOUND));

        verificationService.verifyAccount(account, hash, command.otpCode());
        return null;
    }
}
