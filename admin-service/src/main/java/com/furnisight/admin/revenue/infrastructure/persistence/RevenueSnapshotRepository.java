package com.furnisight.admin.revenue.infrastructure.persistence;

import com.furnisight.admin.revenue.infrastructure.persistence.RevenueSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RevenueSnapshotRepository extends JpaRepository<RevenueSnapshot, UUID> {
    Optional<RevenueSnapshot> findByYearMonth(String yearMonth);
    List<RevenueSnapshot> findAllByOrderByYearMonthAsc();
}
