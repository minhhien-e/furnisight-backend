package com.furnisight.promotion.application.service.marketing;

import com.furnisight.promotion.application.port.CatalogStockPort;

import com.furnisight.promotion.application.dto.*;
import com.furnisight.promotion.application.port.in.query.*;
import com.furnisight.promotion.application.port.in.usecase.DispatchCampaignUseCase;
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
public class DispatchCampaignService implements DispatchCampaignUseCase {

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
    
    public void dispatchCampaign(DispatchCampaignQuery query) {
        List<MarketingChannel> channels = helper.parseChannels(helper.split(query.getCampaign().getChannels()));
        List<MarketingNotificationGateway.Recipient> recipients = helper.resolveRecipients(
                query.getCampaign().getTargetType().name(), helper.split(query.getCampaign().getTargetUserIds()), query.getCampaign().getSegmentKey(), channels);
        Map<String, Object> metadata = helper.buildVoucherMetadata(query.getCampaign().getVoucherId());
        MarketingNotificationGateway.DispatchResult result = notificationGateway.send(
                helper.defaultText(query.getCampaign().getNotificationTitle(), query.getCampaign().getName()),
                helper.defaultText(query.getCampaign().getNotificationBody(), "Bạn vừa nhận ưu đãi mới từ LuxNest."),
                "/account/vouchers",
                channels,
                recipients,
                metadata);
        query.getCampaign().setSentCount(result.sentCount());
        query.getCampaign().setStatus(query.getCampaign().getScheduleType() == MarketingSendType.SCHEDULED || query.getCampaign().getScheduleType() == MarketingSendType.NOW ? CampaignStatus.SENT : CampaignStatus.DRAFT);
        query.getCampaign().setDispatchedAt(LocalDateTime.now());
        campaignRepository.save(query.getCampaign());
        helper.logBatch("CAMPAIGN", query.getCampaign().getId(), query.getCampaign().getNotificationTitle(), query.getCampaign().getNotificationBody(), channels, recipients, result);
    }
}
