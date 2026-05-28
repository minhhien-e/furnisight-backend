package com.furnisight.user.domain.repository.profile;

import com.furnisight.user.domain.entities.profile.UserAddress;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAddressRepository {
    void save(UserAddress address);
    List<UserAddress> findByAccountId(UUID accountId);
    Optional<UserAddress> findById(UUID id);
    void delete(UserAddress address);
    void deleteById(UUID id);
}
