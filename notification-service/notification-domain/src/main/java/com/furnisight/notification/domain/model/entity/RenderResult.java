package com.furnisight.notification.domain.model.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RenderResult {
    private String title;
    private String body;
}
