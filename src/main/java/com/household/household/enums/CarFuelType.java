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
    PETROL_ELECTRIC_HYBRID,
    STRONG_HYBRID,
    DIESEL_HYBRID,
    DIESEL_MILD_HYBRID,
    FLEX_FUEL,
    FLEX_FUEL_E20_E100,
    OTHER;

    public static CarFuelType fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        // Remove brackets / parentheses: e.g. "Flex-Fuel (E20–E100)" -> "Flex-Fuel  E20–E100 "
        String cleaned = text.trim().replaceAll("[()\\[\\]{}]", " ");

        // Normalize hyphens, en-dash (\u2013), em-dash (\u2014), slashes, plus, spaces
        String normalized = cleaned.toUpperCase()
                .replaceAll("[-–—/\\\\|+]+", "_")
                .replaceAll("\\s+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");

        for (CarFuelType f : CarFuelType.values()) {
            if (f.name().equals(normalized)) {
                return f;
            }
        }

        if (normalized.equals("FLEX_FUEL") || normalized.equals("FLEXIBLE_FUEL") || normalized.equals("FLEX")) {
            return FLEX_FUEL_E20_E100;
        }

        throw new IllegalArgumentException("Unknown CarFuelType: " + text);
    }
}
