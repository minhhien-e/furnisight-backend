package com.furnisight.user.presentation.web.rest.controller.profile;

import com.furnisight.user.application.common.port.in.CurrentUserProvider;
import com.furnisight.user.application.address.port.in.command.AddAddressCommand;
import com.furnisight.user.application.address.port.in.command.DeleteAddressCommand;
import com.furnisight.user.application.address.port.in.command.SetDefaultAddressCommand;
import com.furnisight.user.application.address.port.in.projection.AddressProjection;
import com.furnisight.user.application.address.port.in.usecase.AddAddressUseCase;
import com.furnisight.user.application.address.port.in.usecase.DeleteAddressUseCase;
import com.furnisight.user.application.address.port.in.usecase.GetAddressesUseCase;
import com.furnisight.user.application.address.port.in.usecase.SetDefaultAddressUseCase;
import com.furnisight.user.presentation.web.rest.dto.response.AddressResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/profile/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddAddressUseCase addAddressUseCase;
    private final GetAddressesUseCase getAddressesUseCase;
    private final SetDefaultAddressUseCase setDefaultAddressUseCase;
    private final DeleteAddressUseCase deleteAddressUseCase;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public ResponseEntity<List<AddressResponse>> getAddresses() {
        UUID accountId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(getAddressesUseCase.execute(accountId).stream()
                .map(AddressResponse::from)
                .toList());
    }

    @PostMapping
    public ResponseEntity<AddressResponse> addAddress(@RequestBody AddAddressCommand command) {
        UUID accountId = currentUserProvider.getCurrentUserId();
        command.setAccountId(accountId);
        AddressProjection newAddress = addAddressUseCase.execute(command);
        return ResponseEntity.ok(AddressResponse.from(newAddress));
    }

    @PostMapping("/{id}/default")
    public ResponseEntity<List<AddressResponse>> setDefaultAddress(@PathVariable UUID id) {
        UUID accountId = currentUserProvider.getCurrentUserId();
        setDefaultAddressUseCase.execute(new SetDefaultAddressCommand(accountId, id));
        return ResponseEntity.ok(getAddressesUseCase.execute(accountId).stream()
                .map(AddressResponse::from)
                .toList());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable UUID id) {
        UUID accountId = currentUserProvider.getCurrentUserId();
        deleteAddressUseCase.execute(new DeleteAddressCommand(accountId, id));
        return ResponseEntity.ok().build();
    }
}
