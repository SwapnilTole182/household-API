package com.household.household.enums;

public enum AcInverterType {
    INVERTER,
    NON_INVERTER;

    public static AcInverterType fromString(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String normalized = text.trim().toUpperCase().replace(" ", "_").replace("-", "_");
        for (AcInverterType b : AcInverterType.values()) {
            if (b.name().equals(normalized)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unknown Inverter/Non-Inverter Type: " + text);
    }
}
