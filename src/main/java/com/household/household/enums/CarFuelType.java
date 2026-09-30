package com.household.household.enums;

public enum CarFuelType {
    PETROL,
    DIESEL,
    CNG,
    ELECTRIC,
    HYBRID,
    LPG,
    PETROL_CNG,
    PETROL_MILD_HYBRID,
    PETROL_DIESEL,
    PETROL_STRONG_HYBRID,
    PETROL_ELECTRIC,
    STRONG_HYBRID,
    OTHER;

    public static CarFuelType fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String normalized = text.trim().toUpperCase()
                .replace(" ", "_")
                .replace("-", "_")
                .replace("+", "_");
        
        for (CarFuelType f : CarFuelType.values()) {
            if (f.name().equals(normalized)) {
                return f;
            }
        }
        throw new IllegalArgumentException("Unknown CarFuelType: " + text);
    }
}
