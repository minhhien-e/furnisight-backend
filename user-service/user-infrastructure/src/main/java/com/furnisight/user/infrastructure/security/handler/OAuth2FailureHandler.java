package com.furnisight.user.infrastructure.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        // Log the real cause so we can debug it
        log.error("[OAuth2] Authentication failed: {}", exception.getMessage(), exception);
        if (exception.getCause() != null) {
            log.error("[OAuth2] Caused by: {}", exception.getCause().getMessage(), exception.getCause());
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
            "{ \"error\": \"oauth2_authentication_failed\", " +
                "\"message\": \"" + exception.getMessage() + "\" }"
        );
        response.getWriter().flush();
    }
}
