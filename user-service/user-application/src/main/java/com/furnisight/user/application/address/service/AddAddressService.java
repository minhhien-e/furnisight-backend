package com.furnisight.user.application.address.service;

import com.furnisight.user.application.address.port.in.command.AddAddressCommand;
import com.furnisight.user.application.address.port.in.response.AddressResponse;
import com.furnisight.user.application.address.port.in.usecase.AddAddressUseCase;
import com.furnisight.user.domain.entities.profile.UserAddress;
import com.furnisight.user.domain.repository.profile.UserAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddAddressService implements AddAddressUseCase {

    private final UserAddressRepository addressRepository;

    @Override
    @Transactional
    public AddressResponse execute(AddAddressCommand command) {
        List<UserAddress> existingAddresses = addressRepository.findByAccountId(command.getAccountId());
        
        boolean isDefault = command.isDefault() || existingAddresses.isEmpty();

        if (isDefault) {
            for (UserAddress addr : existingAddresses) {
                if (addr.isDefault()) {
                    addr.setDefault(false);
                    addressRepository.save(addr);
                }
            }
        }

        UserAddress newAddress = new UserAddress(
                command.getAccountId(),
                command.getFullName(),
                command.getPhone(),
                command.getProvinceCode(),
                command.getProvinceName(),
                command.getWardCode(),
                command.getWardName(),
                command.getDetail(),
                command.getType(),
                isDefault
        );

        addressRepository.save(newAddress);
        return mapToResponse(newAddress);
    }

    private AddressResponse mapToResponse(UserAddress address) {
        return AddressResponse.builder()
                .id(address.getId())
                .fullName(address.getFullName())
                .phone(address.getPhone())
                .provinceCode(address.getProvinceCode())
                .provinceName(address.getProvinceName())
                .wardCode(address.getWardCode())
                .wardName(address.getWardName())
                .detail(address.getDetail())
                .type(address.getType())
                .isDefault(address.isDefault())
                .build();
    }
}
