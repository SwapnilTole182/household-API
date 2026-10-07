package com.household.household.enums;

public enum CarPremiumStatus {
    PREMIUM,
    NON_PREMIUM;

    public static CarPremiumStatus fromString(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String normalized = text.trim().toUpperCase().replace("-", "_").replace(" ", "_");
        if (normalized.equals("NON_PREMIUM")) {
            return NON_PREMIUM;
        } else if (normalized.equals("PREMIUM")) {
            return PREMIUM;
        }
        throw new IllegalArgumentException("Unknown premium status: " + text);
    }
}
