package com.household.household.enums;

public enum WashingMachineBrand {
    BOSCH,
    ELECTROLUX,
    GODREJ,
    HAIER,
    IFB,
    LG,
    LLOYD,
    PANASONIC,
    SAMSUNG,
    SIEMENS,
    VOLTAS_BEKO,
    WHIRLPOOL,
    OTHER;

    public static WashingMachineBrand fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String normalized = text.trim().toUpperCase()
                .replace(" ", "_")
                .replace("-", "_")
                .replace("VOLTASBEKO", "VOLTAS_BEKO");
        for (WashingMachineBrand b : WashingMachineBrand.values()) {
            if (b.name().equals(normalized)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unknown Washing Machine Brand: " + text);
    }
}
