package com.furnisight.admin.controller.dto;

public record UpdateAdminOrderRequest(String status, String statusLabel, String trackingCode, String note) {
}
