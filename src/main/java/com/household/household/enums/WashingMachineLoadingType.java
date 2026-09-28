package com.household.household.enums;

public enum WashingMachineLoadingType {
    FRONT_LOAD,
    TOP_LOAD;

    public static WashingMachineLoadingType fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String normalized = text.trim().toUpperCase().replace(" ", "_").replace("-", "_");
        for (WashingMachineLoadingType lt : WashingMachineLoadingType.values()) {
            if (lt.name().equals(normalized)) {
                return lt;
            }
        }
        throw new IllegalArgumentException("Unknown Loading Type: " + text);
    }
}
