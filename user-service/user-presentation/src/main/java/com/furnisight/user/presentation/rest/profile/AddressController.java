package com.furnisight.user.presentation.rest.profile;

import com.furnisight.user.application.common.port.in.CurrentUserProvider;
import com.furnisight.user.application.address.port.in.command.AddAddressCommand;
import com.furnisight.user.application.address.port.in.command.DeleteAddressCommand;
import com.furnisight.user.application.address.port.in.command.SetDefaultAddressCommand;
import com.furnisight.user.application.address.port.in.projection.AddressProjection;
import com.furnisight.user.application.address.port.in.usecase.AddAddressUseCase;
import com.furnisight.user.application.address.port.in.usecase.DeleteAddressUseCase;
import com.furnisight.user.application.address.port.in.usecase.GetAddressesUseCase;
import com.furnisight.user.application.address.port.in.usecase.SetDefaultAddressUseCase;
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
    public ResponseEntity<List<AddressProjection>> getAddresses() {
        UUID accountId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(getAddressesUseCase.execute(accountId));
    }

    @PostMapping
    public ResponseEntity<AddressProjection> addAddress(@RequestBody AddAddressCommand command) {
        UUID accountId = currentUserProvider.getCurrentUserId();
        command.setAccountId(accountId);
        AddressProjection newAddress = addAddressUseCase.execute(command);
        return ResponseEntity.ok(newAddress);
    }

    @PostMapping("/{id}/default")
    public ResponseEntity<Void> setDefaultAddress(@PathVariable UUID id) {
        UUID accountId = currentUserProvider.getCurrentUserId();
        setDefaultAddressUseCase.execute(new SetDefaultAddressCommand(accountId, id));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable UUID id) {
        UUID accountId = currentUserProvider.getCurrentUserId();
        deleteAddressUseCase.execute(new DeleteAddressCommand(accountId, id));
        return ResponseEntity.ok().build();
    }
}
