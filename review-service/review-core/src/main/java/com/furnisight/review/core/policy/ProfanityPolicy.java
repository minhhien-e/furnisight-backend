package com.furnisight.review.core.policy;

import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ProfanityPolicy {
    private static final List<String> FORBIDDEN_WORDS = List.of("tày", "mixi");

    public boolean containsForbiddenWords(String content) {
        return FORBIDDEN_WORDS.stream().anyMatch(content.toLowerCase()::contains);
    }
}
