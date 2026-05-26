package com.furnisight.order.adapter.in.web.rest;

import com.furnisight.order.adapter.in.web.provider.ClientIpProvider;
import com.furnisight.order.application.payment.port.in.command.CreatePaymentCommand;
import com.furnisight.order.application.payment.port.in.usecase.CreatePaymentUseCase;
import com.furnisight.order.application.payment.port.in.usecase.ProcessPaymentCallbackUseCase;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final CreatePaymentUseCase createPaymentUseCase;
    private final ProcessPaymentCallbackUseCase processPaymentCallbackUseCase;
    private final ClientIpProvider clientIpProvider;

    @PostMapping("/{paymentMethod}/create")
    public ResponseEntity<String> createPayment(@PathVariable String paymentMethod, @RequestParam String orderCode,
            HttpServletRequest request) {
        String clientIp = clientIpProvider.getClientIp(request);
        CreatePaymentCommand command = CreatePaymentCommand.builder()
                .paymentMethod(paymentMethod)
                .orderCode(orderCode)
                .clientIp(clientIp)
                .build();

        String paymentUrl = createPaymentUseCase.createPaymentUrl(command);
        return ResponseEntity.ok(paymentUrl);
    }

    @org.springframework.beans.factory.annotation.Value("${frontend.url}")
    private String frontendUrl;

    @GetMapping("/{paymentMethod}/callback")
    public ResponseEntity<Void> processPaymentCallback(@PathVariable String paymentMethod,
            @RequestParam Map<String, String> params) {
        boolean success = processPaymentCallbackUseCase.processCallback(paymentMethod, params);
        if (success) {
            return ResponseEntity.status(302).location(URI.create(frontendUrl + "/payment/success")).build();
        } else {
            return ResponseEntity.status(302).location(URI.create(frontendUrl + "/payment/failure")).build();
        }
    }
}
