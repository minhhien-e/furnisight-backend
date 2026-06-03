package com.furnisight.admin.service;

import com.furnisight.admin.controller.dto.InventoryWarningSettingsResponse;
import com.furnisight.admin.controller.dto.SaveInventoryWarningSettingsRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AdminInventorySettingsService {

    private volatile int defaultThreshold;
    private final ConcurrentHashMap<String, Integer> variantThresholds = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final Path settingsFile;

    public AdminInventorySettingsService(
            @Value("${admin.inventory.default-low-stock-threshold:5}") int defaultThreshold,
            @Value("${admin.inventory.settings-file:/app/data/inventory-warning-settings.json}") String settingsFile,
            ObjectMapper objectMapper) {
        this.defaultThreshold = sanitize(defaultThreshold);
        this.settingsFile = Path.of(settingsFile);
        this.objectMapper = objectMapper;
        loadFromDisk();
    }

    public InventoryWarningSettingsResponse getSettings() {
        return new InventoryWarningSettingsResponse(defaultThreshold, Map.copyOf(variantThresholds));
    }

    public int defaultThreshold() {
        return defaultThreshold;
    }

    public InventoryWarningSettingsResponse saveSettings(SaveInventoryWarningSettingsRequest request) {
        if (request.defaultThreshold() != null) {
            defaultThreshold = sanitize(request.defaultThreshold());
        }
        if (request.variantThresholds() != null) {
            request.variantThresholds().forEach((variantId, threshold) -> {
                if (variantId == null || variantId.isBlank()) {
                    return;
                }
                if (threshold == null || threshold <= 0) {
                    variantThresholds.remove(variantId);
                } else {
                    variantThresholds.put(variantId, sanitize(threshold));
                }
            });
        }
        saveToDisk();
        return getSettings();
    }

    public int thresholdForVariant(String variantId) {
        if (variantId == null || variantId.isBlank()) {
            return defaultThreshold;
        }
        return variantThresholds.getOrDefault(variantId, defaultThreshold);
    }

    private int sanitize(int value) {
        return Math.max(1, Math.min(value, 9999));
    }

    private void loadFromDisk() {
        if (!Files.exists(settingsFile)) {
            return;
        }
        try {
            InventorySettingsFile settings = objectMapper.readValue(settingsFile.toFile(), InventorySettingsFile.class);
            if (settings.defaultThreshold() != null) {
                defaultThreshold = sanitize(settings.defaultThreshold());
            }
            if (settings.variantThresholds() != null) {
                settings.variantThresholds().forEach((variantId, threshold) -> {
                    if (variantId != null && !variantId.isBlank() && threshold != null && threshold > 0) {
                        variantThresholds.put(variantId, sanitize(threshold));
                    }
                });
            }
        } catch (Exception ignored) {
            variantThresholds.clear();
        }
    }

    private void saveToDisk() {
        try {
            Path parent = settingsFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            objectMapper.writeValue(settingsFile.toFile(),
                    new InventorySettingsFile(defaultThreshold, Map.copyOf(variantThresholds)));
        } catch (Exception ignored) {
            // Settings still apply in memory if the filesystem is unavailable.
        }
    }

    private record InventorySettingsFile(Integer defaultThreshold, Map<String, Integer> variantThresholds) {
    }
}
