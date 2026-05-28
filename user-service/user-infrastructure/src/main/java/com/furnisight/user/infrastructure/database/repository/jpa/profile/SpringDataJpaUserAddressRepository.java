package com.furnisight.user.infrastructure.database.repository.jpa.profile;

import com.furnisight.user.domain.entities.profile.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataJpaUserAddressRepository extends JpaRepository<UserAddress, UUID> {
    List<UserAddress> findByAccountId(UUID accountId);
}
