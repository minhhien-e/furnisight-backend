package com.furnisight.media.api.interceptor;

import com.furnisight.media.api.service.IdempotencyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class IdempotencyInterceptor implements HandlerInterceptor {
    private final IdempotencyService idempotencyService;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws IOException {

        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String key = request.getHeader("Idempotency-Key");

        if (key == null) {
            return true;
        }

        String cachedResponse = idempotencyService.get(key);

        if (cachedResponse != null) {
            response.setContentType("application/json");
            response.getWriter().write(cachedResponse);
            return false;
        }

        return true;
    }
}
