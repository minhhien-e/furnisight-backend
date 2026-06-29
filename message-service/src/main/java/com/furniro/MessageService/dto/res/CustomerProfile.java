package com.furniro.MessageService.dto.res;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerProfile {
    private Integer buyerId;
    private String accountId;
    private String buyerName;
    private String buyerEmail;
    private String buyerAvatarUrl;
}
