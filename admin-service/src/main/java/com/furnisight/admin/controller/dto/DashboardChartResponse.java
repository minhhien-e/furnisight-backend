package com.furnisight.admin.controller.dto;

import java.util.List;

public record DashboardChartResponse(List<String> labels, List<Double> data) {
}
