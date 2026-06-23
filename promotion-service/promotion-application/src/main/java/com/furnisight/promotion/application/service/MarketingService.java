package com.furnisight.promotion.application.service;

import com.furnisight.promotion.application.dto.*;
import com.furnisight.promotion.application.port.*;
import com.furnisight.promotion.domain.entities.*;
import com.furnisight.promotion.domain.enums.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketingService {
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

    public PageResponse<MarketingCampaignDto> getCampaigns(String query, String status) {
        List<MarketingCampaignDto> items = campaignRepository.findAll().stream()
                .filter(c -> matches(c.getName(), query) || matches(voucherCode(c.getVoucherId()), query))
                .filter(c -> matchesStatus(c.getStatus().name(), status))
                .sorted(Comparator.comparing(MarketingCampaign::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toCampaignDto)
                .toList();
        return unpaged(items);
    }

    @Transactional
    public MarketingCampaignDto createCampaign(SaveMarketingCampaignCommand command) {
        MarketingCampaign campaign = MarketingCampaign.builder().id(UUID.randomUUID()).build();
        applyCampaign(campaign, command);
        campaign = campaignRepository.save(campaign);
        if (campaign.getScheduleType() == MarketingSendType.NOW) {
            dispatchCampaign(campaign);
        }
        return toCampaignDto(campaignRepository.findById(campaign.getId()).orElse(campaign));
    }

    @Transactional
    public MarketingCampaignDto updateCampaign(UUID id, SaveMarketingCampaignCommand command) {
        MarketingCampaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));
        applyCampaign(campaign, command);
        campaign = campaignRepository.save(campaign);
        if (campaign.getScheduleType() == MarketingSendType.NOW && campaign.getDispatchedAt() == null) {
            dispatchCampaign(campaign);
        }
        return toCampaignDto(campaignRepository.findById(campaign.getId()).orElse(campaign));
    }

    @Transactional
    public void deleteCampaign(UUID id) {
        campaignRepository.deleteById(id);
    }

    public PageResponse<MarketingComboDto> getCombos(String query, String status) {
        List<MarketingComboDto> items = comboRepository.findAll().stream()
                .filter(c -> matches(c.getName(), query))
                .filter(c -> matchesStatus(comboStatus(c), status))
                .sorted(Comparator.comparing(PromotionCombo::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toComboDto)
                .toList();
        return unpaged(items);
    }

    public List<MarketingComboDto> getActiveCombos() {
        List<MarketingComboDto> result = new ArrayList<>();
        int page = 0;
        while (true) {
            var dbPage = comboRepository.findActivePage(LocalDateTime.now(), page, 24, "default");
            result.addAll(enrichStock(dbPage.items()));
            page++;
            if (page >= dbPage.totalPages()) return result;
        }
    }

    public PageResponse<MarketingComboDto> getPublicCombos(boolean availableOnly, String sort, Integer page, Integer size) {
        int safePage = Math.max(0, page == null ? 0 : page);
        int safeSize = Math.max(1, Math.min(24, size == null ? 6 : size));
        LocalDateTime now = LocalDateTime.now();
        if (!availableOnly) {
            var dbPage = comboRepository.findActivePage(now, safePage, safeSize, sort);
            return new PageResponse<>(enrichStock(dbPage.items()), dbPage.totalPages(), dbPage.totalElements(), safePage, safeSize);
        }

        int wantedFrom = safePage * safeSize;
        int availableCount = 0;
        int scanPage = 0;
        List<MarketingComboDto> requested = new ArrayList<>();
        while (true) {
            var dbPage = comboRepository.findActivePage(now, scanPage, 24, sort);
            for (MarketingComboDto combo : enrichStock(dbPage.items())) {
                if (!combo.isAvailable()) continue;
                if (availableCount >= wantedFrom && requested.size() < safeSize) requested.add(combo);
                availableCount++;
            }
            scanPage++;
            if (scanPage >= dbPage.totalPages()) break;
        }
        return new PageResponse<>(requested, (int) Math.ceil((double) availableCount / safeSize),
                availableCount, safePage, safeSize);
    }

    public ValidateComboResponse validateCombo(ValidateComboCommand command) {
        UUID comboId = parseUuid(command.getComboId());
        if (comboId == null) {
            return invalidCombo(command.getComboId(), "Missing combo id");
        }
        PromotionCombo combo = comboRepository.findById(comboId).orElse(null);
        if (combo == null) {
            return invalidCombo(command.getComboId(), "Combo not found");
        }
        if (!combo.isActive()) {
            return invalidCombo(combo.getId().toString(), "Combo inactive");
        }
        LocalDateTime now = LocalDateTime.now();
        if (combo.getStartDate() != null && combo.getStartDate().isAfter(now)) {
            return invalidCombo(combo.getId().toString(), "Combo not started");
        }
        if (combo.getEndDate() != null && combo.getEndDate().isBefore(now)) {
            return invalidCombo(combo.getId().toString(), "Combo expired");
        }
        List<PromotionComboItem> requiredItems = comboItemRepository.findByComboId(combo.getId());
        if (requiredItems.isEmpty()) {
            return invalidCombo(combo.getId().toString(), "Combo has no items");
        }
        List<ValidateComboCommand.Item> requestedItems = command.getItems() == null ? List.of() : command.getItems();
        boolean hasRequiredItems = requiredItems.stream().allMatch(item -> {
            int requestedQuantity = requestedItems.stream()
                    .filter(requested -> item.getProductId().equals(requested.getProductId()))
                    .filter(requested -> !hasText(item.getVariantId()) || item.getVariantId().equals(requested.getVariantId()))
                    .mapToInt(requested -> Math.max(0, requested.getQuantity() == null ? 0 : requested.getQuantity()))
                    .sum();
            return requestedQuantity >= Math.max(1, item.getQuantity());
        });
        if (!hasRequiredItems) {
            return invalidCombo(combo.getId().toString(), "Cart does not contain all combo items");
        }
        Map<String, CatalogStockPort.StockItem> stock = lookupStock(requiredItems);
        boolean hasStock = requiredItems.stream().allMatch(item -> {
            var live = stock.get(stockKey(item.getProductId(), item.getVariantId()));
            return live != null && live.stockQuantity() != null
                    && live.stockQuantity() >= Math.max(1, item.getQuantity());
        });
        if (!hasStock) {
            return invalidCombo(combo.getId().toString(), "Combo items are out of stock");
        }
        return ValidateComboResponse.builder()
                .valid(true)
                .comboId(combo.getId().toString())
                .comboName(combo.getName())
                .originalAmount(combo.getOriginalAmount())
                .finalAmount(combo.getFinalAmount())
                .comboDiscount(combo.getSavedAmount())
                .message("Combo valid")
                .build();
    }

    @Transactional
    public MarketingComboDto createCombo(SaveMarketingComboCommand command) {
        PromotionCombo combo = PromotionCombo.builder().id(UUID.randomUUID()).usedCount(0).build();
        applyCombo(combo, command);
        combo = comboRepository.save(combo);
        saveComboItems(combo, command);
        recalculateCombo(combo.getId());
        return toComboDto(comboRepository.findById(combo.getId()).orElse(combo));
    }

    @Transactional
    public MarketingComboDto updateCombo(UUID id, SaveMarketingComboCommand command) {
        PromotionCombo combo = comboRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Combo not found"));
        applyCombo(combo, command);
        comboRepository.save(combo);
        comboItemRepository.deleteByComboId(id);
        saveComboItems(combo, command);
        recalculateCombo(id);
        return toComboDto(comboRepository.findById(id).orElse(combo));
    }

    @Transactional
    public void deleteCombo(UUID id) {
        comboItemRepository.deleteByComboId(id);
        comboRepository.deleteById(id);
    }

    public PageResponse<MarketingNotificationDto> getNotifications(String query, String status) {
        List<MarketingNotificationDto> items = notificationRepository.findAll().stream()
                .filter(n -> matches(n.getTitle(), query))
                .filter(n -> matchesStatus(n.getStatus().name(), status))
                .sorted(Comparator.comparing(MarketingNotification::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toNotificationDto)
                .toList();
        return unpaged(items);
    }

    private <T> PageResponse<T> unpaged(List<T> items) {
        return new PageResponse<>(items, 1, items.size(), 1, items.size());
    }

    @Transactional
    public MarketingNotificationDto createNotification(SaveMarketingNotificationCommand command) {
        MarketingNotification notification = MarketingNotification.builder().id(UUID.randomUUID()).build();
        applyNotification(notification, command);
        notification = notificationRepository.save(notification);
        if (notification.getSendType() == MarketingSendType.NOW) {
            dispatchNotification(notification);
        }
        return toNotificationDto(notificationRepository.findById(notification.getId()).orElse(notification));
    }

    @Transactional
    public MarketingNotificationDto updateNotification(UUID id, SaveMarketingNotificationCommand command) {
        MarketingNotification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        applyNotification(notification, command);
        notification = notificationRepository.save(notification);
        if (notification.getSendType() == MarketingSendType.NOW && notification.getDispatchedAt() == null) {
            dispatchNotification(notification);
        }
        return toNotificationDto(notificationRepository.findById(id).orElse(notification));
    }

    @Transactional
    public void deleteNotification(UUID id) {
        notificationRepository.deleteById(id);
    }

    @Transactional
    public MarketingNotificationGateway.DispatchResult publishVoucher(UUID voucherId, PublishVoucherCommand command) {
        Promotion voucher = promotionRepository.findById(voucherId)
                .orElseThrow(() -> new IllegalArgumentException("Voucher not found"));
        List<MarketingChannel> channels = parseChannels(command.getChannels());
        List<MarketingNotificationGateway.Recipient> recipients = resolveRecipients(
                command.getTargetType(), command.getTargetUserIds(), command.getSegmentKey(), channels);
        for (MarketingNotificationGateway.Recipient recipient : recipients) {
            try {
                userVoucherRepository.save(UserVoucher.builder()
                        .id(UUID.randomUUID())
                        .userId(recipient.userId())
                        .promotionId(voucher.getId())
                        .used(false)
                        .savedAt(LocalDateTime.now())
                        .build());
                logDispatch("VOUCHER_PUBLISH", voucherId, recipient.userId(), null, DispatchStatus.ACCEPTED, command.getTitle(), "Voucher granted", null);
            } catch (Exception ex) {
                logDispatch("VOUCHER_PUBLISH", voucherId, recipient.userId(), null, DispatchStatus.SKIPPED, command.getTitle(), "Voucher grant skipped", ex.getMessage());
            }
        }
        String title = hasText(command.getTitle()) ? command.getTitle() : "Bạn vừa nhận voucher " + voucher.getCode();
        String body = hasText(command.getBody()) ? command.getBody() : "Voucher " + voucher.getName() + " đã sẵn sàng trong tài khoản của bạn.";
        MarketingNotificationGateway.DispatchResult result = notificationGateway.send(title, body, "/account/vouchers", channels, recipients);
        for (MarketingNotificationGateway.Recipient recipient : recipients) {
            for (MarketingChannel channel : channels) {
                logDispatch("VOUCHER_PUBLISH", voucherId, recipient.userId(), channel, result.failedCount() > 0 ? DispatchStatus.ACCEPTED : DispatchStatus.SENT, title, body, null);
            }
        }
        return result;
    }

    @Scheduled(fixedDelayString = "${promotion.marketing.dispatch-delay-ms:60000}")
    @Transactional
    public void dispatchDueJobs() {
        LocalDateTime now = LocalDateTime.now();
        campaignRepository.findDueScheduled(CampaignStatus.SCHEDULED, now).forEach(this::dispatchCampaign);
        notificationRepository.findDueScheduled(CampaignStatus.SCHEDULED, now).forEach(this::dispatchNotification);
    }

    @Transactional
    public void dispatchCampaign(MarketingCampaign campaign) {
        List<MarketingChannel> channels = parseChannels(split(campaign.getChannels()));
        List<MarketingNotificationGateway.Recipient> recipients = resolveRecipients(
                campaign.getTargetType().name(), split(campaign.getTargetUserIds()), campaign.getSegmentKey(), channels);
        MarketingNotificationGateway.DispatchResult result = notificationGateway.send(
                defaultText(campaign.getNotificationTitle(), campaign.getName()),
                defaultText(campaign.getNotificationBody(), "Bạn vừa nhận ưu đãi mới từ LuxNest."),
                "/account/vouchers",
                channels,
                recipients);
        campaign.setSentCount(result.sentCount());
        campaign.setStatus(campaign.getScheduleType() == MarketingSendType.SCHEDULED || campaign.getScheduleType() == MarketingSendType.NOW ? CampaignStatus.SENT : CampaignStatus.DRAFT);
        campaign.setDispatchedAt(LocalDateTime.now());
        campaignRepository.save(campaign);
        logBatch("CAMPAIGN", campaign.getId(), campaign.getNotificationTitle(), campaign.getNotificationBody(), channels, recipients, result);
    }

    @Transactional
    public void dispatchNotification(MarketingNotification notification) {
        List<MarketingChannel> channels = parseChannels(split(notification.getChannels()));
        List<MarketingNotificationGateway.Recipient> recipients = resolveRecipients(
                notification.getTargetType().name(), split(notification.getTargetUserIds()), notification.getSegmentKey(), channels);
        MarketingNotificationGateway.DispatchResult result = notificationGateway.send(
                notification.getTitle(),
                notification.getBody(),
                "/notifications",
                channels,
                recipients);
        notification.setSentCount(result.sentCount());
        notification.setStatus(notification.getSendType() == MarketingSendType.DRAFT ? CampaignStatus.DRAFT : CampaignStatus.SENT);
        notification.setDispatchedAt(LocalDateTime.now());
        notificationRepository.save(notification);
        logBatch("NOTIFICATION", notification.getId(), notification.getTitle(), notification.getBody(), channels, recipients, result);
    }

    private void applyCampaign(MarketingCampaign campaign, SaveMarketingCampaignCommand command) {
        campaign.setName(requireText(command.getName(), "Missing campaign name"));
        campaign.setVoucherId(parseUuid(command.getVoucherId()));
        campaign.setTargetType(parseRequiredEnum(command.getTargetType(), "Invalid target type", MarketingTargetType.class));
        validateTarget(campaign.getTargetType(), command.getTargetUserIds(), command.getSegmentKey());
        campaign.setTargetUserIds(join(command.getTargetUserIds()));
        campaign.setSegmentKey(validatedSegment(campaign.getTargetType(), command.getSegmentKey()));
        campaign.setChannels(join(parseChannels(command.getChannels()).stream().map(Enum::name).toList()));
        campaign.setScheduleType(parseEnum(command.getScheduleType(), MarketingSendType.NOW, MarketingSendType.class));
        campaign.setScheduledAt(command.getScheduledAt());
        campaign.setNotificationTitle(command.getNotificationTitle());
        campaign.setNotificationBody(command.getNotificationBody());
        campaign.setActive(command.getActive() == null || command.getActive());
        campaign.setStatus(campaign.getScheduleType() == MarketingSendType.SCHEDULED ? CampaignStatus.SCHEDULED : CampaignStatus.DRAFT);
    }

    private void applyNotification(MarketingNotification notification, SaveMarketingNotificationCommand command) {
        notification.setTitle(requireText(command.getTitle(), "Missing notification title"));
        notification.setBody(defaultText(command.getBody(), ""));
        notification.setTargetType(parseRequiredEnum(command.getTargetType(), "Invalid target type", MarketingTargetType.class));
        validateTarget(notification.getTargetType(), command.getTargetUserIds(), command.getSegmentKey());
        notification.setTargetUserIds(join(command.getTargetUserIds()));
        notification.setSegmentKey(validatedSegment(notification.getTargetType(), command.getSegmentKey()));
        notification.setChannels(join(parseChannels(command.getChannels()).stream().map(Enum::name).toList()));
        notification.setSendType(parseEnum(command.getSendType(), MarketingSendType.NOW, MarketingSendType.class));
        notification.setScheduledAt(command.getScheduledAt());
        notification.setRelatedVoucherId(parseUuid(command.getRelatedVoucherId()));
        notification.setActive(command.getActive() == null || command.getActive());
        notification.setStatus(switch (notification.getSendType()) {
            case NOW -> CampaignStatus.RUNNING;
            case SCHEDULED -> CampaignStatus.SCHEDULED;
            case DRAFT -> CampaignStatus.DRAFT;
        });
    }

    private void applyCombo(PromotionCombo combo, SaveMarketingComboCommand command) {
        if (command.getItems() == null || command.getItems().isEmpty()) {
            throw new IllegalArgumentException("Combo requires at least one item");
        }
        combo.setName(requireText(command.getName(), "Missing combo name"));
        combo.setDescription(defaultText(command.getDescription(), ""));
        combo.setImageMediaId(command.getImageMediaId());
        combo.setImageUrl(defaultText(command.getImageUrl(), ""));
        combo.setDiscountType(parseEnum(command.getDiscountType(), ComboDiscountType.PERCENTAGE, ComboDiscountType.class));
        combo.setDiscountValue(Math.max(0, command.getDiscountValue() == null ? 0 : command.getDiscountValue()));
        combo.setStartDate(command.getStartDate());
        combo.setEndDate(command.getEndDate());
        combo.setActive(command.getActive() == null || command.getActive());
    }

    private void saveComboItems(PromotionCombo combo, SaveMarketingComboCommand command) {
        for (SaveMarketingComboCommand.Item item : command.getItems()) {
            comboItemRepository.save(PromotionComboItem.builder()
                    .id(UUID.randomUUID())
                    .comboId(combo.getId())
                    .productId(requireText(item.getProductId(), "Missing product id"))
                    .variantId(item.getVariantId())
                    .productName(defaultText(item.getProductName(), item.getProductId()))
                    .sku(defaultText(item.getSku(), ""))
                    .categoryName(defaultText(item.getCategoryName(), ""))
                    .image(defaultText(item.getImage(), "box"))
                    .price(Math.max(0, item.getPrice() == null ? 0 : item.getPrice()))
                    .quantity(Math.max(1, item.getQuantity() == null ? 1 : item.getQuantity()))
                    .snapshotMissing(item.getPrice() == null || item.getProductName() == null)
                    .build());
        }
    }

    private void recalculateCombo(UUID comboId) {
        PromotionCombo combo = comboRepository.findById(comboId).orElseThrow();
        double original = comboItemRepository.findByComboId(comboId).stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        double finalAmount = switch (combo.getDiscountType()) {
            case PERCENTAGE -> original * (1 - combo.getDiscountValue() / 100.0);
            case FIXED_AMOUNT -> original - combo.getDiscountValue();
            case FIXED_COMBO_PRICE -> combo.getDiscountValue();
        };
        finalAmount = Math.max(0, Math.min(original, finalAmount));
        combo.setOriginalAmount(original);
        combo.setFinalAmount(finalAmount);
        combo.setSavedAmount(Math.max(0, original - finalAmount));
        comboRepository.save(combo);
    }

    private MarketingCampaignDto toCampaignDto(MarketingCampaign campaign) {
        List<String> channels = split(campaign.getChannels());
        return MarketingCampaignDto.builder()
                .id(campaign.getId().toString())
                .name(campaign.getName())
                .voucherId(campaign.getVoucherId() == null ? null : campaign.getVoucherId().toString())
                .voucherCode(voucherCode(campaign.getVoucherId()))
                .targetType(campaign.getTargetType().name())
                .targetUserIds(split(campaign.getTargetUserIds()))
                .targetLabel(targetLabel(campaign.getTargetType(), campaign.getSegmentKey(), split(campaign.getTargetUserIds()).size()))
                .segmentKey(campaign.getSegmentKey())
                .channels(channels)
                .channelLabels(channels)
                .scheduleType(campaign.getScheduleType().name())
                .scheduledAt(campaign.getScheduledAt())
                .notificationTitle(campaign.getNotificationTitle())
                .notificationBody(campaign.getNotificationBody())
                .status(campaign.getStatus().name())
                .sentCount(campaign.getSentCount())
                .active(campaign.isActive())
                .createdAt(campaign.getCreatedAt())
                .build();
    }

    private MarketingNotificationDto toNotificationDto(MarketingNotification notification) {
        List<String> channels = split(notification.getChannels());
        return MarketingNotificationDto.builder()
                .id(notification.getId().toString())
                .title(notification.getTitle())
                .body(notification.getBody())
                .targetType(notification.getTargetType().name())
                .targetUserIds(split(notification.getTargetUserIds()))
                .targetLabel(targetLabel(notification.getTargetType(), notification.getSegmentKey(), split(notification.getTargetUserIds()).size()))
                .segmentKey(notification.getSegmentKey())
                .channels(channels)
                .channelLabels(channels)
                .sendType(notification.getSendType().name())
                .scheduledAt(notification.getScheduledAt())
                .relatedVoucherId(notification.getRelatedVoucherId() == null ? null : notification.getRelatedVoucherId().toString())
                .status(notification.getStatus().name())
                .sentCount(notification.getSentCount())
                .active(notification.isActive())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    private MarketingComboDto toComboDto(PromotionCombo combo) {
        return toComboDto(combo, comboItemRepository.findByComboId(combo.getId()), Map.of());
    }

    private MarketingComboDto toComboDto(PromotionCombo combo, List<PromotionComboItem> comboItems,
                                          Map<String, CatalogStockPort.StockItem> stock) {
        List<MarketingComboDto.Item> items = comboItems.stream()
                .map(item -> MarketingComboDto.Item.builder()
                        .productId(item.getProductId())
                        .variantId(item.getVariantId())
                        .productName(item.getProductName())
                        .sku(item.getSku())
                        .categoryName(item.getCategoryName())
                        .image(item.getImage())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .snapshotMissing(item.isSnapshotMissing())
                        .stockQuantity(stockQuantity(stock, item))
                        .available(itemAvailable(stock, item))
                        .build())
                .toList();
        return MarketingComboDto.builder()
                .id(combo.getId().toString())
                .name(combo.getName())
                .description(combo.getDescription())
                .imageMediaId(combo.getImageMediaId())
                .imageUrl(combo.getImageUrl())
                .discountType(combo.getDiscountType().name())
                .discountValue(combo.getDiscountValue())
                .startDate(combo.getStartDate())
                .endDate(combo.getEndDate())
                .active(combo.isActive())
                .items(items)
                .itemCount(items.size())
                .originalAmount(combo.getOriginalAmount())
                .finalAmount(combo.getFinalAmount())
                .savedAmount(combo.getSavedAmount())
                .usedCount(combo.getUsedCount())
                .status(comboStatus(combo))
                .available(!items.isEmpty() && items.stream().allMatch(MarketingComboDto.Item::isAvailable))
                .createdAt(combo.getCreatedAt())
                .build();
    }

    private List<MarketingComboDto> enrichStock(List<PromotionCombo> combos) {
        if (combos == null || combos.isEmpty()) return List.of();
        Set<UUID> ids = combos.stream().map(PromotionCombo::getId).collect(java.util.stream.Collectors.toSet());
        Map<UUID, List<PromotionComboItem>> itemsByCombo = comboItemRepository.findByComboIds(ids).stream()
                .collect(java.util.stream.Collectors.groupingBy(PromotionComboItem::getComboId));
        List<PromotionComboItem> allItems = itemsByCombo.values().stream().flatMap(Collection::stream).toList();
        Map<String, CatalogStockPort.StockItem> stock = lookupStock(allItems);
        return combos.stream().map(combo -> toComboDto(combo,
                itemsByCombo.getOrDefault(combo.getId(), List.of()), stock)).toList();
    }

    private Map<String, CatalogStockPort.StockItem> lookupStock(Collection<PromotionComboItem> items) {
        return catalogStockPort.getStockItems(items.stream()
                .map(item -> new CatalogStockPort.LookupItem(item.getProductId(), item.getVariantId()))
                .distinct().toList());
    }

    private Integer stockQuantity(Map<String, CatalogStockPort.StockItem> stock, PromotionComboItem item) {
        var live = stock.get(stockKey(item.getProductId(), item.getVariantId()));
        return live == null ? null : live.stockQuantity();
    }

    private boolean itemAvailable(Map<String, CatalogStockPort.StockItem> stock, PromotionComboItem item) {
        Integer quantity = stockQuantity(stock, item);
        return quantity != null && quantity >= Math.max(1, item.getQuantity());
    }

    private String stockKey(String productId, String variantId) {
        return defaultText(productId, "") + "::" + defaultText(variantId, "");
    }

    private List<MarketingNotificationGateway.Recipient> resolveRecipients(
            String targetType, List<String> targetUserIds, String segmentKey, List<MarketingChannel> channels) {
        MarketingTargetType type = parseRequiredEnum(targetType, "Invalid target type", MarketingTargetType.class);
        validateTarget(type, targetUserIds, segmentKey);
        List<UUID> userIds = switch (type) {
            case MANUAL -> targetGateway.filterEligibleUserIds(targetUserIds.stream()
                    .map(this::parseUuid).filter(Objects::nonNull).distinct().toList());
            case ALL -> targetGateway.getAllActiveUserIds();
            case SEGMENT -> targetGateway.getSegmentUserIds(validatedSegment(type, segmentKey));
        };
        if (channels.contains(MarketingChannel.EMAIL)) {
            return targetGateway.getUsersByIds(userIds);
        }
        return userIds.stream().map(id -> new MarketingNotificationGateway.Recipient(id, null, null)).toList();
    }

    private void logBatch(String sourceType, UUID sourceId, String title, String message, List<MarketingChannel> channels, List<MarketingNotificationGateway.Recipient> recipients, MarketingNotificationGateway.DispatchResult result) {
        DispatchStatus status = result.failedCount() > 0 ? DispatchStatus.ACCEPTED : DispatchStatus.SENT;
        for (MarketingNotificationGateway.Recipient recipient : recipients) {
            for (MarketingChannel channel : channels) {
                logDispatch(sourceType, sourceId, recipient.userId(), channel, status, title, message, null);
            }
        }
    }

    private void logDispatch(String sourceType, UUID sourceId, UUID userId, MarketingChannel channel, DispatchStatus status, String title, String message, String error) {
        dispatchLogRepository.save(MarketingDispatchLog.builder()
                .id(UUID.randomUUID())
                .sourceType(sourceType)
                .sourceId(sourceId)
                .userId(userId)
                .channel(channel)
                .status(status)
                .title(title)
                .message(message)
                .error(error)
                .build());
    }

    private ValidateComboResponse invalidCombo(String comboId, String message) {
        return ValidateComboResponse.builder()
                .valid(false)
                .comboId(comboId)
                .comboDiscount(0)
                .message(message)
                .build();
    }

    private String voucherCode(UUID voucherId) {
        if (voucherId == null) return "";
        return promotionRepository.findById(voucherId).map(Promotion::getCode).orElse("");
    }

    private boolean matches(String value, String query) {
        return query == null || query.isBlank() || (value != null && value.toLowerCase(Locale.ROOT).contains(query.trim().toLowerCase(Locale.ROOT)));
    }

    private boolean matchesStatus(String value, String status) {
        return status == null || status.isBlank() || value.equalsIgnoreCase(status);
    }

    private String comboStatus(PromotionCombo combo) {
        if (!combo.isActive()) return "DRAFT";
        LocalDateTime now = LocalDateTime.now();
        if (combo.getStartDate() != null && combo.getStartDate().isAfter(now)) return "SCHEDULED";
        if (combo.getEndDate() != null && combo.getEndDate().isBefore(now)) return "EXPIRED";
        return "ACTIVE";
    }

    private boolean isWithinWindow(PromotionCombo combo) {
        LocalDateTime now = LocalDateTime.now();
        return (combo.getStartDate() == null || !combo.getStartDate().isAfter(now))
                && (combo.getEndDate() == null || !combo.getEndDate().isBefore(now));
    }

    private String targetLabel(MarketingTargetType type, String segmentKey, int manualCount) {
        return switch (type) {
            case MANUAL -> manualCount + " người dùng đã chọn";
            case ALL -> "Toàn bộ người dùng";
            case SEGMENT -> switch (defaultText(segmentKey, "")) {
                case "NEW_USERS" -> "Khách mới đăng ký";
                case "INACTIVE_30D" -> "Chưa mua hàng 30 ngày";
                case "ABANDONED_CART" -> "Giỏ hàng chưa checkout";
                default -> "Theo điều kiện";
            };
        };
    }

    private List<MarketingChannel> parseChannels(List<String> channels) {
        if (channels == null || channels.isEmpty()) {
            throw new IllegalArgumentException("At least one marketing channel is required");
        }
        return channels.stream()
                .map(value -> parseRequiredEnum(value, "Invalid marketing channel", MarketingChannel.class))
                .distinct().toList();
    }

    private void validateTarget(MarketingTargetType type, List<String> targetUserIds, String segmentKey) {
        if (type == MarketingTargetType.MANUAL) {
            long validIds = targetUserIds == null ? 0 : targetUserIds.stream()
                    .map(this::parseUuid).filter(Objects::nonNull).distinct().count();
            if (validIds == 0) throw new IllegalArgumentException("Manual target requires user ids");
        }
        if (type == MarketingTargetType.SEGMENT) validatedSegment(type, segmentKey);
    }

    private String validatedSegment(MarketingTargetType type, String segmentKey) {
        if (type != MarketingTargetType.SEGMENT) return null;
        return parseRequiredEnum(segmentKey, "Invalid marketing segment", MarketingSegment.class).name();
    }

    private String join(List<String> values) {
        if (values == null || values.isEmpty()) return "";
        return String.join(",", values.stream().filter(this::hasText).map(String::trim).toList());
    }

    private List<String> split(String value) {
        if (!hasText(value)) return List.of();
        return Arrays.stream(value.split(",")).map(String::trim).filter(this::hasText).toList();
    }

    private UUID parseUuid(String value) {
        if (!hasText(value)) return null;
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String requireText(String value, String message) {
        if (!hasText(value)) throw new IllegalArgumentException(message);
        return value.trim();
    }

    private String defaultText(String value, String fallback) {
        return hasText(value) ? value.trim() : fallback;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private <T extends Enum<T>> T parseEnum(String value, T fallback, Class<T> enumClass) {
        if (!hasText(value)) return fallback;
        try {
            return Enum.valueOf(enumClass, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }

    private <T extends Enum<T>> T parseRequiredEnum(String value, String message, Class<T> enumClass) {
        if (!hasText(value)) throw new IllegalArgumentException(message);
        try {
            return Enum.valueOf(enumClass, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(message + ": " + value);
        }
    }
}
