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
public class GetAdminVouchersQuery {
private String query;
private  String type;
private  String status;
}
