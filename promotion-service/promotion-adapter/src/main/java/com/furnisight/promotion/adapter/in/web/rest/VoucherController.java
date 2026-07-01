package com.furnisight.promotion.adapter.in.web.rest;

import com.furnisight.promotion.application.common.CurrentUserProvider;
import com.furnisight.promotion.application.dto.PromotionDto;
import com.furnisight.promotion.domain.common.PageResponse;
import com.furnisight.promotion.application.dto.RecommendVouchersCommand;
import com.furnisight.promotion.application.dto.RecommendVouchersResponse;
import com.furnisight.promotion.application.dto.ValidateVoucherCommand;
import com.furnisight.promotion.application.dto.ValidateVoucherResponse;
import com.furnisight.promotion.application.port.in.usecase.*;
import com.furnisight.promotion.application.port.in.query.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/vouchers")
@RequiredArgsConstructor
public class VoucherController {
    private final GetAvailableVouchersUseCase getAvailableVouchersUseCase;
    private final GetPublicVouchersUseCase getPublicVouchersUseCase;
    private final RecommendVouchersUseCase recommendVouchersUseCase;
    private final ValidateVoucherUseCase validateVoucherUseCase;
    private final SaveUserVoucherUseCase saveUserVoucherUseCase;
    private final CurrentUserProvider currentUserProvider;
    private final PromotionTranslationService promotionTranslationService;

    @GetMapping("/user")
    public ResponseEntity<List<PromotionDto>> getAvailableVouchers(
            @RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage,
            @RequestParam(name = "lang", required = false) String lang) {
        UUID userId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(promotionTranslationService.localizePromotions(
                getAvailableVouchersUseCase.getAvailableVouchers(GetAvailableVouchersQuery.builder().userId(userId).build()),
                resolveLocale(lang, acceptLanguage)
        ));
    }

    @GetMapping("/public")
    public ResponseEntity<PageResponse<PromotionDto>> getPublicVouchers(
            @RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage,
            @RequestParam(name = "lang", required = false) String lang,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false, defaultValue = "all") String filter) {
        return ResponseEntity.ok(promotionTranslationService.localizePromotionPage(
                getPublicVouchersUseCase.getPublicVouchers(GetPublicVouchersQuery.builder().userId(currentUserIdOrNull()).page(page).size(size).filter(filter).build()),
                resolveLocale(lang, acceptLanguage)
        ));
    }

    @PostMapping("/recommend")
    public ResponseEntity<RecommendVouchersResponse> recommendVouchers(
            @RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage,
            @RequestParam(name = "lang", required = false) String lang,
            @RequestBody RecommendVouchersCommand command) {
        return ResponseEntity.ok(promotionTranslationService.localizeRecommendResponse(
                recommendVouchersUseCase.recommendVouchers(RecommendVouchersQuery.builder().userId(currentUserProvider.getCurrentUserId()).command(command).build()),
                resolveLocale(lang, acceptLanguage)
        ));
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidateVoucherResponse> validateVoucher(
            @RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage,
            @RequestParam(name = "lang", required = false) String lang,
            @RequestBody ValidateVoucherCommand command) {
        command.setUserId(currentUserProvider.getCurrentUserId());
        return ResponseEntity.ok(promotionTranslationService.localizeValidateVoucherResponse(
                validateVoucherUseCase.validateVoucher(command),
                resolveLocale(lang, acceptLanguage)
        ));
    }

    @PostMapping("/{code}/save")
    public ResponseEntity<Void> saveVoucher(@PathVariable String code) {
        saveUserVoucherUseCase.saveVoucher(SaveUserVoucherQuery.builder().userId(currentUserProvider.getCurrentUserId()).code(code).build());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/save")
    public ResponseEntity<Void> saveVoucherByQuery(@RequestParam String code) {
        saveUserVoucherUseCase.saveVoucher(SaveUserVoucherQuery.builder().userId(currentUserProvider.getCurrentUserId()).code(code).build());
        return ResponseEntity.ok().build();
    }

    private UUID currentUserIdOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            return null;
        }
        try {
            return UUID.fromString(auth.getPrincipal().toString());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private String resolveLocale(String lang, String acceptLanguage) {
        return promotionTranslationService.normalizeLang(
                lang != null && !lang.isBlank() ? lang : acceptLanguage
        );
    }
}
