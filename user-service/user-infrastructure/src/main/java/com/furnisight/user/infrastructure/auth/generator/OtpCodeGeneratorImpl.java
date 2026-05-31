package com.furnisight.user.infrastructure.auth.generator;

import com.furnisight.user.domain.services.identity.generator.OtpCodeGenerator;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class OtpCodeGeneratorImpl implements OtpCodeGenerator {

    private static final SecureRandom random = new SecureRandom();

    @Override
    public String generateOtpCode() {
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
