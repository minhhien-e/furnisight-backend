package com.furnisight.order.domain.valueobjects.order;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDetail {
    private String paymentMethod;
    private String paymentStatus;
    private Double paidAmount;
    private LocalDateTime paidAt;
}
