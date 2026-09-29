package com.household.household.enums;

public enum WashingMachineBrand {
    ACER,
    BOSCH,
    BPL,
    CANDES,
    CROMA,
    ELECTROLUX,
    GODREJ,
    HAIER,
    HISENSE,
    IFB,
    INTEX,
    KELVINATOR,
    KENSTAR,
    LG,
    LLOYD,
    MARQ,
    MICROMAX,
    MIDEA,
    MITASHI,
    ONIDA,
    PANASONIC,
    SAMSUNG,
    SIEMENS,
    SINGER,
    TCL,
    THOMSON,
    TOSHIBA,
    VIDEOCON,
    VOLTAS,
    VOLTAS_BEKO,
    WHIRLPOOL,
    WHITE_WESTINGHOUSE,
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
