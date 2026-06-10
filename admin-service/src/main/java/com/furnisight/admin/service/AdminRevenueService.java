package com.furnisight.admin.service;

import com.furnisight.admin.controller.dto.AdminRevenueMonthlyRow;
import com.furnisight.admin.controller.dto.AdminRevenueResponse;
import com.furnisight.admin.controller.dto.DashboardKpiResponse;
import com.furnisight.admin.entity.RevenueSnapshot;
import com.furnisight.admin.integration.GrpcAdminOrderClient;
import com.furnisight.admin.order.MonthlyRevenue;
import com.furnisight.admin.order.RevenueSummaryResponse;
import com.furnisight.admin.repository.RevenueSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
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
public class AdminRevenueService {

    private static final int SNAPSHOT_MONTHS = 12;
    private static final int STALE_MINUTES   = 5;

    private final GrpcAdminOrderClient grpcAdminOrderClient;
    private final RevenueSnapshotRepository snapshotRepository;

    public AdminRevenueResponse getRevenueSummary() {
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
        List<DashboardKpiResponse> kpis = List.of(
                new DashboardKpiResponse("revenue_total", "Tổng doanh thu",
                        formatCurrency(totalRevenue), "", "", true, "gold", "trendingUp"),
                new DashboardKpiResponse("revenue_month", "Doanh thu tháng này",
                        formatCurrency(revenueThisMonth), "",
                        buildMomSubtitle(latestMonth.getMomChangePct()), true, "blue", "calendar"),
                new DashboardKpiResponse("orders_total", "Tổng đơn hàng",
                        String.valueOf(totalOrders), "", "", true, "green", "box"),
                new DashboardKpiResponse("orders_month", "Đơn tháng này",
                        String.valueOf(ordersThisMonth), "", "", true, "default", "shoppingCart")
        );

        // Biểu đồ
        List<String> monthLabels = recent.stream().map(RevenueSnapshot::getLabel).toList();
        List<Double> monthData   = recent.stream()
                .map(s -> Math.round(s.getTotalRevenue() / 1_000_000D * 100D) / 100D)
                .toList();

        // Bảng chi tiết tháng
        List<AdminRevenueMonthlyRow> rows = recent.stream()
                .sorted((a, b) -> b.getYearMonth().compareTo(a.getYearMonth())) // mới nhất trước
                .map(this::toMonthlyRow)
                .toList();

        String snapshotAt = latestMonth.getSnapshotAt() != null
                ? latestMonth.getSnapshotAt().format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"))
                : "";

        return new AdminRevenueResponse(kpis, monthLabels, monthData, rows, snapshotAt);
    }

    // ---------------------------------------------------------------------------
    // Snapshot rebuild
    // ---------------------------------------------------------------------------

    private void rebuildSnapshots() {
        try {
            RevenueSummaryResponse grpcResponse = grpcAdminOrderClient.getRevenueSummary(SNAPSHOT_MONTHS);
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

    private AdminRevenueMonthlyRow toMonthlyRow(RevenueSnapshot s) {
        String mom = formatMom(s.getMomChangePct());
        String momClass = s.getMomChangePct() != null && s.getMomChangePct() >= 0 ? "up" : "down";
        return new AdminRevenueMonthlyRow(
                s.getLabel(),
                s.getOrderCount(),
                formatCurrency(s.getTotalRevenue()),
                mom,
                momClass,
                "—",
                "—"
        );
    }

    private String formatCurrency(double value) {
        if (value == 0) return "0đ";
        NumberFormat formatter = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
        return formatter.format(Math.round(value)) + "đ";
    }

    private String formatMom(Double pct) {
        if (pct == null) return "—";
        return (pct >= 0 ? "+" : "") + String.format("%.1f", pct) + "%";
    }

    private String buildMomSubtitle(Double pct) {
        if (pct == null) return "";
        return (pct >= 0 ? "+" : "") + String.format("%.1f", pct) + "% so tháng trước";
    }

    private AdminRevenueResponse emptyResponse() {
        return new AdminRevenueResponse(List.of(), List.of(), List.of(), List.of(), "");
    }
}
