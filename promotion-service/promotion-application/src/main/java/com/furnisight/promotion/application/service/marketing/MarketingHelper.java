package com.furnisight.promotion.application.service.marketing;

import com.furnisight.promotion.domain.common.PageResponse;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;

import com.furnisight.promotion.application.port.MarketingNotificationGateway;
import com.furnisight.promotion.application.port.MarketingTargetGateway;
import com.furnisight.promotion.application.port.CatalogStockPort;

import com.furnisight.promotion.application.dto.*;
import com.furnisight.promotion.domain.repository.promotion.*;
import com.furnisight.promotion.domain.repository.marketing.*;
import com.furnisight.promotion.domain.entities.*;
import com.furnisight.promotion.domain.enums.*;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
public class MarketingHelper {

    private final MarketingNotificationGateway notificationGateway;
    private final MarketingTargetGateway targetGateway;
    private final CatalogStockPort catalogStockPort;
    private final PromotionRepository promotionRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final MarketingCampaignRepository campaignRepository;
    private final MarketingNotificationRepository notificationRepository;
    private final PromotionComboRepository comboRepository;
    private final PromotionComboItemRepository comboItemRepository;
    private final MarketingDispatchLogRepository dispatchLogRepository;


public <T> PageResponse<T> unpaged(List<T> items) {
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
        Map<String, Object> metadata = buildVoucherMetadata(voucherId);
        MarketingNotificationGateway.DispatchResult result = notificationGateway.send(title, body, "/account/vouchers", channels, recipients, metadata);
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
        Map<String, Object> metadata = buildVoucherMetadata(campaign.getVoucherId());
        MarketingNotificationGateway.DispatchResult result = notificationGateway.send(
                defaultText(campaign.getNotificationTitle(), campaign.getName()),
                defaultText(campaign.getNotificationBody(), "Bạn vừa nhận ưu đãi mới từ LuxNest."),
                "/account/vouchers",
                channels,
                recipients,
                metadata);
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
        Map<String, Object> metadata = buildVoucherMetadata(notification.getRelatedVoucherId());
        MarketingNotificationGateway.DispatchResult result = notificationGateway.send(
                notification.getTitle(),
                notification.getBody(),
                "/notifications",
                channels,
                recipients,
                metadata);
        notification.setSentCount(result.sentCount());
        notification.setStatus(notification.getSendType() == MarketingSendType.DRAFT ? CampaignStatus.DRAFT : CampaignStatus.SENT);
        notification.setDispatchedAt(LocalDateTime.now());
        notificationRepository.save(notification);
        logBatch("NOTIFICATION", notification.getId(), notification.getTitle(), notification.getBody(), channels, recipients, result);
    }

    public void applyCampaign(MarketingCampaign campaign, SaveMarketingCampaignCommand command) {
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

    public void applyNotification(MarketingNotification notification, SaveMarketingNotificationCommand command) {
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

    public void applyCombo(PromotionCombo combo, SaveMarketingComboCommand command) {
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

    public void saveComboItems(PromotionCombo combo, SaveMarketingComboCommand command) {
        for (SaveMarketingComboCommand.Item item : command.getItems()) {
            comboItemRepository.save(PromotionComboItem.builder()
                    .id(UUID.randomUUID())
                    .comboId(combo.getId())
                    .productId(requireText(item.getProductId(), "Missing product id"))
                    .variantId(item.getVariantId())
                    .productSlug(defaultText(item.getProductSlug(), item.getProductId()))
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

    public void recalculateCombo(UUID comboId) {
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

    public MarketingCampaignDto toCampaignDto(MarketingCampaign campaign) {
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

    public MarketingNotificationDto toNotificationDto(MarketingNotification notification) {
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

    public MarketingComboDto toComboDto(PromotionCombo combo) {
        return toComboDto(combo, comboItemRepository.findByComboId(combo.getId()), Map.of());
    }

    public MarketingComboDto toComboDto(PromotionCombo combo, List<PromotionComboItem> comboItems,
                                          Map<String, CatalogStockPort.StockItem> stock) {
        List<MarketingComboDto.Item> items = comboItems.stream()
                .map(item -> MarketingComboDto.Item.builder()
                        .productId(item.getProductId())
                        .variantId(item.getVariantId())
                        .productSlug(item.getProductSlug())
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

    public List<MarketingComboDto> enrichStock(List<PromotionCombo> combos) {
        if (combos == null || combos.isEmpty()) return List.of();
        Set<UUID> ids = combos.stream().map(PromotionCombo::getId).collect(java.util.stream.Collectors.toSet());
        Map<UUID, List<PromotionComboItem>> itemsByCombo = comboItemRepository.findByComboIds(ids).stream()
                .collect(java.util.stream.Collectors.groupingBy(PromotionComboItem::getComboId));
        List<PromotionComboItem> allItems = itemsByCombo.values().stream().flatMap(Collection::stream).toList();
        Map<String, CatalogStockPort.StockItem> stock = lookupStock(allItems);
        return combos.stream().map(combo -> toComboDto(combo,
                itemsByCombo.getOrDefault(combo.getId(), List.of()), stock)).toList();
    }

    public Map<String, CatalogStockPort.StockItem> lookupStock(Collection<PromotionComboItem> items) {
        return catalogStockPort.getStockItems(items.stream()
                .map(item -> new CatalogStockPort.LookupItem(item.getProductId(), item.getVariantId()))
                .distinct().toList());
    }

    public Integer stockQuantity(Map<String, CatalogStockPort.StockItem> stock, PromotionComboItem item) {
        var live = stock.get(stockKey(item.getProductId(), item.getVariantId()));
        return live == null ? null : live.stockQuantity();
    }

    public boolean itemAvailable(Map<String, CatalogStockPort.StockItem> stock, PromotionComboItem item) {
        Integer quantity = stockQuantity(stock, item);
        return quantity != null && quantity >= Math.max(1, item.getQuantity());
    }

    public String stockKey(String productId, String variantId) {
        return defaultText(productId, "") + "::" + defaultText(variantId, "");
    }

    public List<MarketingNotificationGateway.Recipient> resolveRecipients(
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

    public void logBatch(String sourceType, UUID sourceId, String title, String message, List<MarketingChannel> channels, List<MarketingNotificationGateway.Recipient> recipients, MarketingNotificationGateway.DispatchResult result) {
        DispatchStatus status = result.failedCount() > 0 ? DispatchStatus.ACCEPTED : DispatchStatus.SENT;
        for (MarketingNotificationGateway.Recipient recipient : recipients) {
            for (MarketingChannel channel : channels) {
                logDispatch(sourceType, sourceId, recipient.userId(), channel, status, title, message, null);
            }
        }
    }

    public void logDispatch(String sourceType, UUID sourceId, UUID userId, MarketingChannel channel, DispatchStatus status, String title, String message, String error) {
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

    public ValidateComboResponse invalidCombo(String comboId, String message) {
        return ValidateComboResponse.builder()
                .valid(false)
                .comboId(comboId)
                .comboDiscount(0)
                .message(message)
                .build();
    }

    public String voucherCode(UUID voucherId) {
        if (voucherId == null) return "";
        return promotionRepository.findById(voucherId).map(Promotion::getCode).orElse("");
    }

    public Map<String, Object> buildVoucherMetadata(UUID voucherId) {
        if (voucherId == null) return null;
        return promotionRepository.findById(voucherId).map(voucher -> {
            Map<String, Object> meta = new HashMap<>();
            meta.put("voucherId", voucher.getId().toString());
            meta.put("voucherCode", voucher.getCode());
            meta.put("voucherName", voucher.getName());
            meta.put("discountValue", voucher.getDiscountValue());
            meta.put("discountType", voucher.getDiscountType().name());
            meta.put("minOrder", voucher.getMinOrder());
            meta.put("maxDiscount", voucher.getMaxDiscount());
            meta.put("validUntil", voucher.getEndDate() != null ? voucher.getEndDate().toString() : null);
            return meta;
        }).orElse(null);
    }

    public boolean matches(String value, String query) {
        return query == null || query.isBlank() || (value != null && value.toLowerCase(Locale.ROOT).contains(query.trim().toLowerCase(Locale.ROOT)));
    }

    public boolean matchesStatus(String value, String status) {
        return status == null || status.isBlank() || value.equalsIgnoreCase(status);
    }

    public String comboStatus(PromotionCombo combo) {
        if (!combo.isActive()) return "DRAFT";
        LocalDateTime now = LocalDateTime.now();
        if (combo.getStartDate() != null && combo.getStartDate().isAfter(now)) return "SCHEDULED";
        if (combo.getEndDate() != null && combo.getEndDate().isBefore(now)) return "EXPIRED";
        return "ACTIVE";
    }

    public boolean isWithinWindow(PromotionCombo combo) {
        LocalDateTime now = LocalDateTime.now();
        return (combo.getStartDate() == null || !combo.getStartDate().isAfter(now))
                && (combo.getEndDate() == null || !combo.getEndDate().isBefore(now));
    }

    public String targetLabel(MarketingTargetType type, String segmentKey, int manualCount) {
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

    public List<MarketingChannel> parseChannels(List<String> channels) {
        if (channels == null || channels.isEmpty()) {
            throw new IllegalArgumentException("At least one marketing channel is required");
        }
        return channels.stream()
                .map(value -> parseRequiredEnum(value, "Invalid marketing channel", MarketingChannel.class))
                .distinct().toList();
    }

    public void validateTarget(MarketingTargetType type, List<String> targetUserIds, String segmentKey) {
        if (type == MarketingTargetType.MANUAL) {
            long validIds = targetUserIds == null ? 0 : targetUserIds.stream()
                    .map(this::parseUuid).filter(Objects::nonNull).distinct().count();
            if (validIds == 0) throw new IllegalArgumentException("Manual target requires user ids");
        }
        if (type == MarketingTargetType.SEGMENT) validatedSegment(type, segmentKey);
    }

    public String validatedSegment(MarketingTargetType type, String segmentKey) {
        if (type != MarketingTargetType.SEGMENT) return null;
        return parseRequiredEnum(segmentKey, "Invalid marketing segment", MarketingSegment.class).name();
    }

    public String join(List<String> values) {
        if (values == null || values.isEmpty()) return "";
        return String.join(",", values.stream().filter(this::hasText).map(String::trim).toList());
    }

    public List<String> split(String value) {
        if (!hasText(value)) return List.of();
        return Arrays.stream(value.split(",")).map(String::trim).filter(this::hasText).toList();
    }

    public UUID parseUuid(String value) {
        if (!hasText(value)) return null;
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public String requireText(String value, String message) {
        if (!hasText(value)) throw new IllegalArgumentException(message);
        return value.trim();
    }

    public String defaultText(String value, String fallback) {
        return hasText(value) ? value.trim() : fallback;
    }

    public boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public <T extends Enum<T>> T parseEnum(String value, T fallback, Class<T> enumClass) {
        if (!hasText(value)) return fallback;
        try {
            return Enum.valueOf(enumClass, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }

    public <T extends Enum<T>> T parseRequiredEnum(String value, String message, Class<T> enumClass) {
        if (!hasText(value)) throw new IllegalArgumentException(message);
        try {
            return Enum.valueOf(enumClass, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(message + ": " + value);
        }
    }

}
