package com.furnisight.user.application.address.service;

import com.furnisight.user.application.address.port.in.command.DeleteAddressCommand;
import com.furnisight.user.application.address.port.in.usecase.DeleteAddressUseCase;
import com.furnisight.user.domain.entities.profile.UserAddress;
import com.furnisight.user.domain.repository.profile.UserAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeleteAddressService implements DeleteAddressUseCase {

    private final UserAddressRepository addressRepository;

    @Override
    @Transactional
    public Void execute(DeleteAddressCommand command) {
        UserAddress address = addressRepository.findById(command.getAddressId())
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));
        
        if (!address.getAccountId().equals(command.getAccountId())) {
            throw new IllegalArgumentException("Unauthorized to delete this address");
        }

        addressRepository.delete(address);

        if (address.isDefault()) {
            List<UserAddress> remaining = addressRepository.findByAccountId(command.getAccountId());
            if (!remaining.isEmpty()) {
                UserAddress newDefault = remaining.get(0);
                newDefault.setDefault(true);
                addressRepository.save(newDefault);
            }
        }
        return null;
    }
}
