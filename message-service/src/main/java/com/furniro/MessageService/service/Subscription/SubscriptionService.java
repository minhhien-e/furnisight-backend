package com.furniro.MessageService.service.Subscription;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.furniro.MessageService.database.entity.Subscription;
import com.furniro.MessageService.database.repository.SubscriptionRepository;
import com.furniro.MessageService.dto.API.AType;
import com.furniro.MessageService.dto.API.ApiType;
import com.furniro.MessageService.dto.req.Subscription.SubscriptionReq;
import com.furniro.MessageService.exception.imp.SubscriptionException;
import com.furniro.MessageService.service.Other.MailService;
import com.furniro.MessageService.util.error.SubscriptionErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubscriptionService {
        
    private final SubscriptionRepository subscriptionRepository;
    private final MailService mailService;

    public ResponseEntity<AType> createSubscribe
        (SubscriptionReq req) {
        // 1. check user subscribed
        if (subscriptionRepository.existsByEmail(req.getEmail())) {
            throw new SubscriptionException(SubscriptionErrorCode.SUBSCRIPTION_ALREADY_EXISTS);
        }

        // 2. Save subscription
        Subscription subscription = Subscription.builder()
                .email(req.getEmail())
                .phone(req.getPhone())
                .fullName(req.getFullName())
                .build();

        subscriptionRepository.save(subscription);

        // 3. Send mail subscription success !
        mailService.sendMailSubscription(req.getEmail(), req.getFullName());
        
        // 4. return result !
        return ResponseEntity.ok(ApiType.success(true));
    }

    public ResponseEntity<AType> getAllSubscribers
        (int page, int size, String sortBy) {
            
        // 1. create pabeable from query string
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));

        // 2. get all subscribers
        Page<Subscription> subscribers = subscriptionRepository.findAll(pageable);

        // 3. return result !
        return ResponseEntity.ok(ApiType.success(subscribers));
    }

    public ResponseEntity<AType> deleteSubscriber
    (Integer id) {
        // 1. check user subscribed
        if (!subscriptionRepository.existsById(id)) {
            throw new SubscriptionException(SubscriptionErrorCode.SUBSCRIPTION_NOT_FOUND);
        }

        // 2. delete subscription
        subscriptionRepository.deleteById(id);

        // 3. return result !
        return ResponseEntity.ok(ApiType.success(true));
    }

}
