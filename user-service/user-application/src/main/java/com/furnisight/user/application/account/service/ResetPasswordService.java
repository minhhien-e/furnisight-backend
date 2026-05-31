package com.furnisight.user.application.account.service;

import com.furnisight.user.application.account.dto.ResetPasswordCommand;
import com.furnisight.user.application.account.port.in.usecase.ResetPasswordUseCase;
import com.furnisight.user.domain.entities.identity.Account;
import com.furnisight.user.domain.enums.identity.VerificationType;
import com.furnisight.user.domain.exceptions.DomainException;
import com.furnisight.user.domain.exceptions.identity.ErrorCode;
import com.furnisight.user.domain.exceptions.identity.NotFoundException;
import com.furnisight.user.domain.repository.identity.AccountRepository;
import com.furnisight.user.domain.repository.identity.OtpVerificationRepository;
import com.furnisight.user.domain.services.identity.account.PasswordService;
import com.furnisight.user.domain.valueobjects.identity.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResetPasswordService implements ResetPasswordUseCase {

    private final PasswordService passwordService;
    private final AccountRepository accountRepository;
    private final OtpVerificationRepository otpVerificationRepository;

    @Override
    @Transactional
    public Void execute(ResetPasswordCommand command) {
        String otpHash = otpVerificationRepository.findHashByEmailAndType(command.email(), VerificationType.PASSWORD_RESET)
                .orElseThrow(() -> new DomainException(ErrorCode.TOKEN_EXPIRED));

        Account account = accountRepository.findByEmail(new Email(command.email()))
                .orElseThrow(() -> new NotFoundException(ErrorCode.ACCOUNT_NOT_FOUND));

        passwordService.resetPassword(account, otpHash, command.token(), command.newPassword());
        return null;
    }
}
