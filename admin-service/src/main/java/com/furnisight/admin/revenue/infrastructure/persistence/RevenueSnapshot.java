package com.furnisight.admin.revenue.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Monthly revenue snapshot for fast admin dashboard loading.
 * Rebuilt on demand when the cached data is stale (> 5 minutes for current month,
 * or simply stored permanently for past months).
 */
@Entity
@Table(name = "revenue_snapshots")
public class RevenueSnapshot {

    @Id
    private UUID id;

    /** Format: "yyyy-MM-dd", e.g. "2026-05-01" */
    @Column(name = "year_month", unique = true, nullable = false, length = 20)
    private String yearMonth;

    @Column(name = "total_revenue", nullable = false)
    private double totalRevenue;

    @Column(name = "order_count", nullable = false)
    private long orderCount;

    /** % change vs previous month, null if first month */
    @Column(name = "mom_change_pct")
    private Double momChangePct;


    @Column(name = "snapshot_at", nullable = false)
    private LocalDateTime snapshotAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (snapshotAt == null) snapshotAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        snapshotAt = LocalDateTime.now();
    }

    // Getters & setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getYearMonth() { return yearMonth; }
    public void setYearMonth(String yearMonth) { this.yearMonth = yearMonth; }
    
    public LocalDateTime getRevenueDate() {
        try {
            return java.time.LocalDate.parse(this.yearMonth).atStartOfDay();
        } catch (Exception e) {
            // Fallback if old data is "yyyy-MM"
            return java.time.YearMonth.parse(this.yearMonth).atDay(1).atStartOfDay();
        }
    }

    public String getFormattedMonth() {
        return getRevenueDate().format(java.time.format.DateTimeFormatter.ofPattern("MM/yyyy"));
    }

    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }

    public long getOrderCount() { return orderCount; }
    public void setOrderCount(long orderCount) { this.orderCount = orderCount; }

    public Double getMomChangePct() { return momChangePct; }
    public void setMomChangePct(Double momChangePct) { this.momChangePct = momChangePct; }


    public LocalDateTime getSnapshotAt() { return snapshotAt; }
    public void setSnapshotAt(LocalDateTime snapshotAt) { this.snapshotAt = snapshotAt; }
}
