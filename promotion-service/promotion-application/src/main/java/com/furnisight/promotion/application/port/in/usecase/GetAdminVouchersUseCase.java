package com.furnisight.promotion.application.port.in.usecase;

import com.furnisight.promotion.application.dto.*;
import com.furnisight.promotion.application.port.in.query.*;
import java.util.List;
import java.util.UUID;

public interface GetAdminVouchersUseCase {
    List<PromotionDto> getAdminVouchers(GetAdminVouchersQuery query);
}
