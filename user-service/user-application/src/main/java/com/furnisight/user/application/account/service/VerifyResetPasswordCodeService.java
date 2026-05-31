package com.furnisight.user.application.account.service;

import com.furnisight.user.application.account.dto.VerifyResetPasswordCodeCommand;
import com.furnisight.user.application.account.port.in.usecase.VerifyResetPasswordCodeUseCase;
import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.enums.identity.VerificationType;
import com.furnisight.user.domain.exceptions.DomainException;
import com.furnisight.user.domain.exceptions.identity.ErrorCode;
import com.furnisight.user.domain.exceptions.identity.NotFoundException;
import com.furnisight.user.domain.repository.identity.AccountRepository;
import com.furnisight.user.domain.repository.identity.OtpVerificationRepository;
import com.furnisight.user.domain.services.identity.account.PasswordService;
import com.furnisight.user.domain.valueobjects.identity.Email;
import com.furnisight.user.domain.services.identity.generator.OtpHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerifyResetPasswordCodeService implements VerifyResetPasswordCodeUseCase {

    private final PasswordService passwordService;
    private final AccountRepository accountRepository;
    private final OtpVerificationRepository otpVerificationRepository;
    private final OtpHasher otpHasher;

    @Override
    public Void execute(VerifyResetPasswordCodeCommand command) {
        String hash = otpHasher.hash(command.token());
        String email = otpVerificationRepository.findEmailByHashAndType(hash, VerificationType.PASSWORD_RESET)
                .orElseThrow(() -> new DomainException(ErrorCode.TOKEN_EXPIRED));

        if (!email.equalsIgnoreCase(command.email())) {
            throw new com.furnisight.user.domain.exceptions.identity.UnauthorizedException(ErrorCode.INVALID_TOKEN);
        }

        Account account = accountRepository.findByEmail(new Email(command.email()))
                .orElseThrow(() -> new NotFoundException(ErrorCode.ACCOUNT_NOT_FOUND));

        passwordService.verifyResetPasswordCode(account, hash, command.token());
        return null;
    }
}
