package com.furnisight.promotion.adapter.out.repository;

import com.furnisight.promotion.adapter.out.repository.jpa.PromotionComboJpaRepository;
import com.furnisight.promotion.domain.repository.promotion.PromotionComboRepository;
import com.furnisight.promotion.domain.common.PageResponse;
import com.furnisight.promotion.domain.entities.PromotionCombo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PromotionComboRepositoryImpl implements PromotionComboRepository {
    private final PromotionComboJpaRepository jpaRepository;
    public List<PromotionCombo> findAll() { return jpaRepository.findAll(); }
    public List<PromotionCombo> findActive() { return jpaRepository.findByActiveTrue(); }
    public PageResponse<PromotionCombo> findActivePage(LocalDateTime now, int page, int size, String sort) {
        Sort dbSort = switch (sort == null ? "default" : sort.toLowerCase()) {
            case "save-desc" -> Sort.by(Sort.Order.desc("savedAmount"), Sort.Order.desc("usedCount"),
                    Sort.Order.desc("createdAt"), Sort.Order.asc("id"));
            case "price-asc" -> Sort.by(Sort.Order.asc("finalAmount"), Sort.Order.asc("id"));
            case "price-desc" -> Sort.by(Sort.Order.desc("finalAmount"), Sort.Order.asc("id"));
            default -> Sort.by(Sort.Order.desc("createdAt"), Sort.Order.asc("id"));
        };
        var result = jpaRepository.findActivePage(now, PageRequest.of(page, size, dbSort));
        return new PageResponse<>(result.getContent(), result.getTotalPages(), result.getTotalElements(), page, size);
    }
    public Optional<PromotionCombo> findById(UUID id) { return jpaRepository.findById(id); }
    public PromotionCombo save(PromotionCombo combo) { return jpaRepository.save(combo); }
    public void deleteById(UUID id) { jpaRepository.deleteById(id); }
}
