package com.household.household.enums;

public enum WashingType {
    FULLY_AUTOMATIC,
    SEMI_AUTOMATIC,
    WASHER_DRYER;

    public static WashingType fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String normalized = text.trim().toUpperCase().replace(" ", "_").replace("-", "_");
        for (WashingType wt : WashingType.values()) {
            if (wt.name().equals(normalized)) {
                return wt;
            }
        }
        throw new IllegalArgumentException("Unknown Washing Type: " + text);
    }
}
