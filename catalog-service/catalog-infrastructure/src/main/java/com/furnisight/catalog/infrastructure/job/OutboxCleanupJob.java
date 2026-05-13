package com.furnisight.catalog.infrastructure.job;

import com.furnisight.catalog.infrastructure.database.repository.jpa.outbox.entity.OutboxStatus;
import com.furnisight.catalog.infrastructure.database.repository.jpa.outbox.OutboxJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxCleanupJob {

    private final OutboxJpaRepository outboxRepository;

    /**
     * Chạy dọn dẹp vào lúc 3h sáng mỗi ngày.
     * Xóa các sự kiện đã gửi thành công (SENT) trước 7 ngày.
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanupSentOutboxMessages() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(7);
        log.info("Bắt đầu dọn dẹp OutboxMessages cũ (status=SENT, processedAt < {})", cutoffTime);

        try {
            int deletedCount = outboxRepository.deleteByStatusAndProcessedAtBefore(OutboxStatus.SENT, cutoffTime);
            log.info("Đã dọn dẹp thành công {} OutboxMessages cũ khỏi database.", deletedCount);
        } catch (Exception e) {
            log.error("Lỗi trong quá trình dọn dẹp OutboxMessages: {}", e.getMessage(), e);
        }
    }
}
