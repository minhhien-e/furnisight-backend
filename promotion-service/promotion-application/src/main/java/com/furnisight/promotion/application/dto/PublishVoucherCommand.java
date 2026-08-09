package com.furnisight.promotion.application.dto;

import lombok.Data;

import java.util.List;

@Data
public class PublishVoucherCommand {
    private String targetType;
    private List<String> targetUserIds;
    private String segmentKey;
    private List<String> channels;
    private String title;
    private String body;
}
