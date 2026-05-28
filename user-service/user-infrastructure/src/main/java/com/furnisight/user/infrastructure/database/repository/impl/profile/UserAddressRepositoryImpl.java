package com.furnisight.user.infrastructure.database.repository.impl.profile;

import com.furnisight.user.domain.entities.profile.UserAddress;
import com.furnisight.user.domain.repository.profile.UserAddressRepository;
import com.furnisight.user.infrastructure.database.repository.jpa.profile.SpringDataJpaUserAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserAddressRepositoryImpl implements UserAddressRepository {

    private final SpringDataJpaUserAddressRepository repository;

    @Override
    public void save(UserAddress address) {
        repository.save(address);
    }

    @Override
    public List<UserAddress> findByAccountId(UUID accountId) {
        return repository.findByAccountId(accountId);
    }

    @Override
    public Optional<UserAddress> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public void delete(UserAddress address) {
        repository.delete(address);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
