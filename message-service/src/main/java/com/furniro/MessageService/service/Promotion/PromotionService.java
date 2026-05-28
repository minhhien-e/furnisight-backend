package com.furniro.MessageService.service.Promotion;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.furniro.MessageService.database.entity.Promotion;
import com.furniro.MessageService.database.entity.Subscription;
import com.furniro.MessageService.database.repository.PromotionRepository;
import com.furniro.MessageService.database.repository.SubscriptionRepository;
import com.furniro.MessageService.dto.API.AType;
import com.furniro.MessageService.dto.API.ApiType;
import com.furniro.MessageService.dto.req.promotion.PromotionReq;
import com.furniro.MessageService.exception.imp.PromotionException;
import com.furniro.MessageService.service.Other.MailService;
import com.furniro.MessageService.util.error.PromotionErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PromotionService {
        
        private final PromotionRepository promotionRepository;
        private final SubscriptionRepository subscriptionRepository;
        private final MailService mailService;

        public ResponseEntity<AType> createPromotion
                (PromotionReq req) {
                // 1. create promotion
                Promotion promotion = Promotion.builder()
                                .code(req.getCode())
                                .title(req.getTitle())
                                .description(req.getDescription())
                                .type(req.getType())
                                .value(req.getValue())
                                .status(req.getStatus())
                                .build();

                // 2. save promotion
                promotionRepository.save(promotion);

                // 3. get all subscribers
                List<Subscription> subscribers = subscriptionRepository.findAll();

                // 4. send mail to all subscribers
                subscribers.forEach(subscriber -> {
                        mailService.sendMailPromotion(
                                        subscriber.getEmail(),
                                        subscriber.getFullName(),
                                        promotion.getTitle(),
                                        promotion.getDescription(),
                                        promotion.getCode());
                });

                log.info("Promotion created successfully: {}", promotion.getCode());

                // 5. return response
                return ResponseEntity.ok(ApiType.success(promotion));
        }

        public ResponseEntity<AType> deletePromotion
                (Integer id) {

                Promotion promotion = promotionRepository.findById(id)
                                .orElseThrow(() -> 
                                    new PromotionException(PromotionErrorCode.PROMOTION_NOT_FOUND)
                                );

                promotionRepository.delete(promotion);

                return ResponseEntity.ok(ApiType.success("Promotion deleted successfully"));
        }

        public ResponseEntity<AType> getAllPromotion
                (int page, int size, String sort) {
                Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
                Page<Promotion> promotions = promotionRepository.findAll(pageable);
                return ResponseEntity.ok(ApiType.success(promotions));
        }

        public ResponseEntity<AType> updatePromotion
                (PromotionReq req) {
                Promotion promotion = promotionRepository.findById(req.getId())
                                .orElseThrow(() -> 
                                    new PromotionException(PromotionErrorCode.PROMOTION_NOT_FOUND)
                                );

                promotion.setCode(req.getCode());
                promotion.setTitle(req.getTitle());
                promotion.setDescription(req.getDescription());
                promotion.setType(req.getType());
                promotion.setValue(req.getValue());
                promotion.setStatus(req.getStatus());

                promotionRepository.save(promotion);

                return ResponseEntity.ok(ApiType.success(promotion));
        }

}
