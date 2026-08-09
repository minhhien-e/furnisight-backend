package com.furnisight.user.application.address.service;

import com.furnisight.user.application.address.port.in.command.SetDefaultAddressCommand;
import com.furnisight.user.application.address.port.in.usecase.SetDefaultAddressUseCase;
import com.furnisight.user.domain.entities.profile.UserAddress;
import com.furnisight.user.domain.repository.profile.UserAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SetDefaultAddressService implements SetDefaultAddressUseCase {

    private final UserAddressRepository addressRepository;

    @Override
    @Transactional
    public Void execute(SetDefaultAddressCommand command) {
        List<UserAddress> existingAddresses = addressRepository.findByAccountId(command.getAccountId());
        for (UserAddress addr : existingAddresses) {
            if (addr.getId().equals(command.getAddressId())) {
                addr.setDefault(true);
            } else if (addr.isDefault()) {
                addr.setDefault(false);
            }
            addressRepository.save(addr);
        }
        return null;
    }
}
