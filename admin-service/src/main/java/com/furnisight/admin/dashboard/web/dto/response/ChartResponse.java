package com.furnisight.admin.dashboard.web.dto.response;

import java.util.List;

public record ChartResponse(List<String> labels, List<Double> data) {
}
