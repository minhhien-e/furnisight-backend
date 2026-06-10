package com.furnisight.admin.revenue.application;

import com.furnisight.admin.order.infrastructure.grpc.AdminOrderGrpcClient;
import com.furnisight.admin.revenue.infrastructure.persistence.RevenueSnapshotRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RevenueServiceTest {

    @Test
    void returnsStableEmptyResponseWhenOrderServiceIsUnavailable() {
        AdminOrderGrpcClient orderClient = mock(AdminOrderGrpcClient.class);
        RevenueSnapshotRepository repository = mock(RevenueSnapshotRepository.class);
        when(repository.findByYearMonth(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(Optional.empty());
        when(repository.findAllByOrderByYearMonthAsc()).thenReturn(List.of());
        when(orderClient.getRevenueSummary(anyInt())).thenThrow(new RuntimeException("unavailable"));

        var response = new RevenueService(orderClient, repository).getRevenueSummary();

        assertThat(response.kpis()).isEmpty();
        assertThat(response.monthlyRows()).isEmpty();
        assertThat(response.topProducts()).isEmpty();
        assertThat(response.snapshotAt()).isEmpty();
    }
}
