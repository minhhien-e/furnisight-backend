package com.furnisight.user.infrastructure.database.repository.impl.identity;

import com.furnisight.user.domain.entities.identity.Ban;
import com.furnisight.user.domain.repository.identity.BanRepository;
import com.furnisight.user.infrastructure.database.repository.jpa.identity.BanJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class BanRepositoryImpl implements BanRepository {
    private final BanJpaRepository banJpaRepository;

    @Override
    public Optional<Ban> findByAccountIdAndIsActiveTrue(UUID accountId) {
        return banJpaRepository.findByAccountIdAndIsActiveTrue(accountId);
    }

    @Override
    public Ban save(Ban ban) {
        return banJpaRepository.save(ban);
    }

    @Override
    public void disableAllByAccountId(UUID accountId) {
        banJpaRepository.disableAllByAccountId(accountId);
    }
}
