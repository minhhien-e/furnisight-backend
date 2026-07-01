package com.furnisight.promotion.application.port.in.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;
import com.furnisight.promotion.application.dto.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetPublicVouchersQuery {
private UUID userId;
private  Integer page;
private  Integer size;
private  String filter;
}
