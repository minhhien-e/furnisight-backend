package com.furnisight.notification.adapter.in.web.dto.preference;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateNotificationProfileRequest {
    
    @NotNull(message = "orderUpdatesEnabled flag is mandatory")
    private Boolean orderUpdatesEnabled;
    
    @NotNull(message = "promotionsEnabled flag is mandatory")
    private Boolean promotionsEnabled;
    
    @NotNull(message = "walletUpdatesEnabled flag is mandatory")
    private Boolean walletUpdatesEnabled;
    
    @NotNull(message = "socialUpdatesEnabled flag is mandatory")
    private Boolean socialUpdatesEnabled;
}
