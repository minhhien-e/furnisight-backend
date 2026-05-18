package com.furnisight.media.core.antivirus;

public record ScanResult(boolean isSafe, String virusName) {
    public static ScanResult safe() {
        return new ScanResult(true, null);
    }

    public static ScanResult infected(String virusName) {
        return new ScanResult(false, virusName);
    }
}
