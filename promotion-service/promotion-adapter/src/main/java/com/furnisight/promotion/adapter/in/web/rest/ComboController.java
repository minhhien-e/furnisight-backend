package com.furnisight.promotion.adapter.in.web.rest;

import com.furnisight.promotion.application.dto.MarketingComboDto;
import com.furnisight.promotion.application.dto.ValidateComboCommand;
import com.furnisight.promotion.application.dto.ValidateComboResponse;
import com.furnisight.promotion.application.service.MarketingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/combos")
public class ComboController {
    private final MarketingService marketingService;

    @GetMapping("/active")
    public ResponseEntity<List<MarketingComboDto>> getActiveCombos() {
        return ResponseEntity.ok(marketingService.getActiveCombos());
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidateComboResponse> validateCombo(@RequestBody ValidateComboCommand command) {
        return ResponseEntity.ok(marketingService.validateCombo(command));
    }
}
