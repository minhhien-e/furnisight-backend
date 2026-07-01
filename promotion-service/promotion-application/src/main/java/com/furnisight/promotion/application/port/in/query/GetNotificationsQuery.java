package com.furnisight.promotion.application.port.in.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;
import com.furnisight.promotion.application.dto.*;
import com.furnisight.promotion.domain.entities.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetNotificationsQuery {
private String query;
private  String status;
}
