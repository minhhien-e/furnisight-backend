package com.furnisight.admin.revenue.application;

import com.furnisight.admin.revenue.web.dto.response.RevenueMonthlyItemResponse;
import com.furnisight.admin.revenue.web.dto.response.RevenueResponse;
import com.furnisight.admin.revenue.web.dto.response.TopProductItemResponse;
import com.furnisight.admin.shared.web.KpiResponse;
import com.furnisight.admin.shared.web.KpiType;
import com.furnisight.admin.revenue.infrastructure.persistence.RevenueSnapshot;
import com.furnisight.admin.order.infrastructure.grpc.AdminOrderGrpcClient;
import com.furnisight.admin.order.MonthlyRevenue;
import com.furnisight.admin.order.RevenueSummaryResponse;
import com.furnisight.admin.revenue.infrastructure.persistence.RevenueSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Service phụ trách endpoint doanh thu riêng biệt của admin.
 *
 * Cơ chế snapshot:
 * - Các tháng trong quá khứ (đã qua) → lưu vĩnh viễn, không rebuild lại.
 * - Tháng hiện tại → rebuild nếu snapshot_at > STALE_MINUTES phút trước.
 * - Khi rebuild: gọi gRPC GetRevenueSummary → lưu kết quả vào revenue_snapshots.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RevenueService {

    private static final int SNAPSHOT_MONTHS = 12;
    private static final int STALE_MINUTES   = 5;

    private final AdminOrderGrpcClient orderClient;
    private final RevenueSnapshotRepository snapshotRepository;

    public RevenueResponse getRevenueSummary() {
        String currentYearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        // Kiểm tra xem snapshot tháng hiện tại có cần refresh không
        Optional<RevenueSnapshot> currentSnapshot = snapshotRepository.findByYearMonth(currentYearMonth);
        boolean needsRebuild = currentSnapshot.isEmpty()
                || currentSnapshot.get().getSnapshotAt().isBefore(
                        LocalDateTime.now().minusMinutes(STALE_MINUTES));

        if (needsRebuild) {
            rebuildSnapshots();
        }

        // Load tất cả snapshots đã lưu (12 tháng gần nhất)
        List<RevenueSnapshot> snapshots = snapshotRepository.findAllByOrderByYearMonthAsc()
                .stream()
                .filter(s -> s.getYearMonth().compareTo(currentYearMonth) <= 0)
                .sorted((a, b) -> a.getYearMonth().compareTo(b.getYearMonth()))
                .toList();

        // Nếu không có snapshot nào, trả về response rỗng
        if (snapshots.isEmpty()) {
            return emptyResponse();
        }

        // Lấy 12 tháng gần nhất
        List<RevenueSnapshot> recent = snapshots.size() > SNAPSHOT_MONTHS
                ? snapshots.subList(snapshots.size() - SNAPSHOT_MONTHS, snapshots.size())
                : snapshots;

        double totalRevenue = snapshots.stream().mapToDouble(RevenueSnapshot::getTotalRevenue).sum();
        long totalOrders    = snapshots.stream().mapToLong(RevenueSnapshot::getOrderCount).sum();

        RevenueSnapshot latestMonth = recent.get(recent.size() - 1);
        double revenueThisMonth = latestMonth.getTotalRevenue();
        long   ordersThisMonth  = latestMonth.getOrderCount();

        // KPI cards
        List<KpiResponse> kpis = List.of(
                new KpiResponse(KpiType.REVENUE_TOTAL, totalRevenue, null),
                new KpiResponse(KpiType.REVENUE_MONTH, revenueThisMonth, latestMonth.getMomChangePct()),
                new KpiResponse(KpiType.ORDERS_TOTAL, totalOrders, null),
                new KpiResponse(KpiType.ORDERS_MONTH, ordersThisMonth, null)
        );

        // Biểu đồ
        List<String> monthLabels = recent.stream().map(RevenueSnapshot::getLabel).toList();
        List<Double> monthData   = recent.stream()
                .map(s -> s.getTotalRevenue())
                .toList();

        // Bảng chi tiết tháng
        List<RevenueMonthlyItemResponse> rows = recent.stream()
                .sorted((a, b) -> b.getYearMonth().compareTo(a.getYearMonth())) // mới nhất trước
                .map(this::toMonthlyRow)
                .toList();

        // Top 5 sản phẩm bán chạy nhất
        List<TopProductItemResponse> topProducts = List.of();
        try {
            var topProductsResponse = orderClient.getTopSellingProducts(5);
            topProducts = topProductsResponse.getProductsList().stream()
                    .map(p -> new TopProductItemResponse(
                            p.getProductId(),
                            p.getProductName(),
                            p.getCategoryName(),
                            p.getImageUrl(),
                            p.getPrice(),
                            p.getSoldCount(),
                            p.getTotalRevenue()
                    ))
                    .toList();
        } catch (Exception ex) {
            log.warn("Failed to fetch top selling products from order-service: {}", ex.getMessage());
        }

        String snapshotAt = latestMonth.getSnapshotAt() != null
                ? latestMonth.getSnapshotAt().format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"))
                : "";

        return new RevenueResponse(kpis, monthLabels, monthData, rows, topProducts, snapshotAt);
    }

    // ---------------------------------------------------------------------------
    // Snapshot rebuild
    // ---------------------------------------------------------------------------

    private void rebuildSnapshots() {
        try {
            RevenueSummaryResponse grpcResponse = orderClient.getRevenueSummary(SNAPSHOT_MONTHS);
            for (MonthlyRevenue monthly : grpcResponse.getMonthlyList()) {
                upsertSnapshot(monthly);
            }
            log.info("Revenue snapshots rebuilt successfully ({} months)", grpcResponse.getMonthlyCount());
        } catch (Exception ex) {
            log.warn("Failed to rebuild revenue snapshots: {}", ex.getMessage());
        }
    }

    private void upsertSnapshot(MonthlyRevenue monthly) {
        RevenueSnapshot snapshot = snapshotRepository.findByYearMonth(monthly.getYearMonth())
                .orElseGet(RevenueSnapshot::new);
        snapshot.setYearMonth(monthly.getYearMonth());
        snapshot.setLabel(monthly.getLabel());
        snapshot.setTotalRevenue(monthly.getRevenue());
        snapshot.setOrderCount(monthly.getOrderCount());
        snapshot.setMomChangePct(monthly.getMomChangePct() == 0 ? null : monthly.getMomChangePct());
        snapshot.setSnapshotAt(LocalDateTime.now());
        snapshotRepository.save(snapshot);
    }

    // ---------------------------------------------------------------------------
    // Formatting helpers
    // ---------------------------------------------------------------------------

    private RevenueMonthlyItemResponse toMonthlyRow(RevenueSnapshot s) {
        return new RevenueMonthlyItemResponse(
                s.getLabel(),
                s.getOrderCount(),
                s.getTotalRevenue(),
                s.getMomChangePct(),
                "—",
                "—"
        );
    }

    private RevenueResponse emptyResponse() {
        return new RevenueResponse(List.of(), List.of(), List.of(), List.of(), List.of(), "");
    }
}
