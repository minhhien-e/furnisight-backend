package com.furnisight.user.application.address.service;

import com.furnisight.user.application.address.port.in.command.UpdateAddressCommand;
import com.furnisight.user.application.address.port.in.response.AddressResponse;
import com.furnisight.user.application.address.port.in.usecase.UpdateAddressUseCase;
import com.furnisight.user.domain.entities.profile.UserAddress;
import com.furnisight.user.domain.repository.profile.UserAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UpdateAddressService implements UpdateAddressUseCase {

    private final UserAddressRepository addressRepository;

    @Override
    @Transactional
    public AddressResponse execute(UpdateAddressCommand command) {
        UserAddress address = addressRepository.findById(command.getAddressId())
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));

        if (!address.getAccountId().equals(command.getAccountId())) {
            throw new IllegalArgumentException("Address not found");
        }

        if (command.isDefault()) {
            List<UserAddress> existingAddresses = addressRepository.findByAccountId(command.getAccountId());
            for (UserAddress addr : existingAddresses) {
                if (!addr.getId().equals(address.getId()) && addr.isDefault()) {
                    addr.setDefault(false);
                    addressRepository.save(addr);
                }
            }
        }

        address.setFullName(command.getFullName());
        address.setPhone(command.getPhone());
        address.setProvinceCode(command.getProvinceCode());
        address.setProvinceName(command.getProvinceName());
        address.setWardCode(command.getWardCode());
        address.setWardName(command.getWardName());
        address.setDetail(command.getDetail());
        address.setType(command.getType());
        address.setDefault(command.isDefault() || address.isDefault());

        addressRepository.save(address);
        return mapToResponse(address);
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
