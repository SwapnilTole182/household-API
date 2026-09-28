package com.household.household.enums;

public enum AcType {
    SPLIT,
    WINDOW,
    CASSETTE,
    FLOOR_STANDING,
    PORTABLE,
    TOWER,
    VERTICOOL;


    public static AcType fromString(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String normalized = text.trim().toUpperCase().replace(" ", "_").replace("-", "_");
        for (AcType b : AcType.values()) {
            if (b.name().equals(normalized)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unknown AC Type: " + text);
    }
}
