package com.household.household.enums;

public enum AcBrand {
    VOLTAS,
    CROMA,
    SHARP,
    LG,
    SAMSUNG,
    DAIKIN,
    LLOYD,
    BLUE_STAR,
    HITACHI,
    PANASONIC,
    CARRIER,
    GODREJ,
    O_GENERAL,
    MITSUBISHI,
    TOSHIBA,
    WHIRLPOOL,
    HAIER,
    IFB,
    TCL,
    HISENSE,
    MIDEA,
    CRUISE,
    AMSTRAD,
    ONIDA,
    BPL,
    MARQ,
    AMAZONBASICS,
    OTHER;

    public static AcBrand fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String normalized = text.trim().toUpperCase().replace(" ", "_").replace("-", "_");
        for (AcBrand b : AcBrand.values()) {
            if (b.name().equals(normalized)) {
                return b;
            }
        }
        // Instead of throwing an exception, we could return OTHER if the brand is not in the list.
        // For strict validation, throwing an exception is better so new brands can be explicitly added.
        throw new IllegalArgumentException("Unknown AcBrand: " + text);
    }
}
