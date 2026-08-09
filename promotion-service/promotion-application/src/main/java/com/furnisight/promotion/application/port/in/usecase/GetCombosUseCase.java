package com.furnisight.promotion.application.port.in.usecase;

import com.furnisight.promotion.domain.common.PageResponse;

import com.furnisight.promotion.application.dto.*;
import com.furnisight.promotion.application.port.in.query.*;
import com.furnisight.promotion.domain.entities.*;
import java.util.List;
import java.util.UUID;

public interface GetCombosUseCase {
    PageResponse<MarketingComboDto> getCombos(GetCombosQuery query);
}
