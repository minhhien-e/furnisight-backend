package com.furnisight.promotion.adapter.out.repository;

import com.furnisight.promotion.adapter.out.repository.jpa.PromotionJpaRepository;
import com.furnisight.promotion.application.port.PromotionRepository;
import com.furnisight.promotion.application.dto.PageResponse;
import com.furnisight.promotion.domain.entities.Promotion;
import com.furnisight.promotion.domain.enums.DiscountType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PromotionRepositoryImpl implements PromotionRepository {
    private final PromotionJpaRepository jpaRepository;

    @Override
    public Optional<Promotion> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Promotion> findByCode(String code) {
        return jpaRepository.findByCode(code);
    }

    @Override
    public List<Promotion> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<Promotion> findAllActive() {
        return jpaRepository.findAllActive();
    }

    @Override
    public PageResponse<Promotion> findPublicActivePage(LocalDateTime now, LocalDateTime expiresBefore,
                                                        boolean shippingOnly, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(
                Sort.Order.asc("endDate").nullsLast(), Sort.Order.asc("code"), Sort.Order.asc("id")));
        boolean checkExpiresBefore = expiresBefore != null;
        LocalDateTime safeExpiresBefore = checkExpiresBefore ? expiresBefore : now;
        var result = jpaRepository.findPublicActivePage(now, checkExpiresBefore, safeExpiresBefore, shippingOnly,
                DiscountType.SHIPPING_CAP, pageable);
        return new PageResponse<>(result.getContent(), result.getTotalPages(), result.getTotalElements(), page, size);
    }

    @Override
    public Promotion save(Promotion promotion) {
        return jpaRepository.save(promotion);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
