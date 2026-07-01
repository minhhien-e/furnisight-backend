package com.furnisight.promotion.application.service.marketing;

import com.furnisight.promotion.application.port.CatalogStockPort;

import com.furnisight.promotion.application.dto.*;
import com.furnisight.promotion.application.port.in.query.*;
import com.furnisight.promotion.application.port.in.usecase.PublishVoucherUseCase;
import com.furnisight.promotion.domain.repository.promotion.*;
import com.furnisight.promotion.domain.repository.marketing.*;
import com.furnisight.promotion.domain.entities.*;
import com.furnisight.promotion.domain.enums.*;
import com.furnisight.promotion.application.port.MarketingNotificationGateway;
import com.furnisight.promotion.application.port.MarketingTargetGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PublishVoucherService implements PublishVoucherUseCase {

    private final MarketingHelper helper;
    private final MarketingCampaignRepository campaignRepository;
    private final MarketingNotificationRepository notificationRepository;
    private final PromotionComboRepository comboRepository;
    private final PromotionComboItemRepository comboItemRepository;
    private final MarketingDispatchLogRepository dispatchLogRepository;
    private final PromotionRepository promotionRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final MarketingNotificationGateway notificationGateway;
    private final MarketingTargetGateway targetGateway;
    private final CatalogStockPort catalogStockPort;
    
    // Inject self for async if needed or just other usecases
    
    

    @Override
    @Transactional
    public MarketingNotificationGateway.DispatchResult publishVoucher(PublishVoucherQuery query) {
        Promotion voucher = promotionRepository.findById(query.getVoucherId())
                .orElseThrow(() -> new IllegalArgumentException("Voucher not found"));
        List<MarketingChannel> channels = helper.parseChannels(query.getCommand().getChannels());
        List<MarketingNotificationGateway.Recipient> recipients = helper.resolveRecipients(
                query.getCommand().getTargetType(), query.getCommand().getTargetUserIds(), query.getCommand().getSegmentKey(), channels);
        for (MarketingNotificationGateway.Recipient recipient : recipients) {
            try {
                userVoucherRepository.save(UserVoucher.builder()
                        .id(UUID.randomUUID())
                        .userId(recipient.userId())
                        .promotionId(voucher.getId())
                        .used(false)
                        .savedAt(LocalDateTime.now())
                        .build());
                helper.logDispatch("VOUCHER_PUBLISH", query.getVoucherId(), recipient.userId(), null, DispatchStatus.ACCEPTED, query.getCommand().getTitle(), "Voucher granted", null);
            } catch (Exception ex) {
                helper.logDispatch("VOUCHER_PUBLISH", query.getVoucherId(), recipient.userId(), null, DispatchStatus.SKIPPED, query.getCommand().getTitle(), "Voucher grant skipped", ex.getMessage());
            }
        }
        String title = helper.hasText(query.getCommand().getTitle()) ? query.getCommand().getTitle() : "Bạn vừa nhận voucher " + voucher.getCode();
        String body = helper.hasText(query.getCommand().getBody()) ? query.getCommand().getBody() : "Voucher " + voucher.getName() + " đã sẵn sàng trong tài khoản của bạn.";
        Map<String, Object> metadata = helper.buildVoucherMetadata(query.getVoucherId());
        MarketingNotificationGateway.DispatchResult result = notificationGateway.send(title, body, "/account/vouchers", channels, recipients, metadata);
        for (MarketingNotificationGateway.Recipient recipient : recipients) {
            for (MarketingChannel channel : channels) {
                helper.logDispatch("VOUCHER_PUBLISH", query.getVoucherId(), recipient.userId(), channel, result.failedCount() > 0 ? DispatchStatus.ACCEPTED : DispatchStatus.SENT, title, body, null);
            }
        }
        return result;
    }
}
