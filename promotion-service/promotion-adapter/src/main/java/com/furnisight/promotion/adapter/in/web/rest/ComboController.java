package com.furnisight.promotion.adapter.in.web.rest;

import com.furnisight.promotion.application.dto.MarketingComboDto;
import com.furnisight.promotion.domain.common.PageResponse;
import com.furnisight.promotion.application.dto.ValidateComboCommand;
import com.furnisight.promotion.application.dto.ValidateComboResponse;
import com.furnisight.promotion.application.port.in.usecase.*;
import com.furnisight.promotion.application.port.in.query.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/combos")
public class ComboController {
    private final GetActiveCombosUseCase getActiveCombosUseCase;
    private final GetPublicCombosUseCase getPublicCombosUseCase;
    private final ValidateComboUseCase validateComboUseCase;
    private final PromotionTranslationService promotionTranslationService;

    @GetMapping("/active")
    public ResponseEntity<List<MarketingComboDto>> getActiveCombos(
            @RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage,
            @RequestParam(name = "lang", required = false) String lang) {
        return ResponseEntity.ok(promotionTranslationService.localizeCombos(
                getActiveCombosUseCase.getActiveCombos(new GetActiveCombosQuery()),
                resolveLocale(lang, acceptLanguage)
        ));
    }

    @GetMapping
    public ResponseEntity<PageResponse<MarketingComboDto>> getPublicCombos(
            @RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage,
            @RequestParam(name = "lang", required = false) String lang,
            @RequestParam(required = false, defaultValue = "false") boolean availableOnly,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(promotionTranslationService.localizeComboPage(
                getPublicCombosUseCase.getPublicCombos(GetPublicCombosQuery.builder().availableOnly(availableOnly).sort(sort).page(page).size(size).build()),
                resolveLocale(lang, acceptLanguage)
        ));
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidateComboResponse> validateCombo(
            @RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage,
            @RequestParam(name = "lang", required = false) String lang,
            @RequestBody ValidateComboCommand command) {
        return ResponseEntity.ok(promotionTranslationService.localizeValidateComboResponse(
                validateComboUseCase.validateCombo(command),
                resolveLocale(lang, acceptLanguage)
        ));
    }

    private String resolveLocale(String lang, String acceptLanguage) {
        return promotionTranslationService.normalizeLang(
                lang != null && !lang.isBlank() ? lang : acceptLanguage
        );
    }
}
