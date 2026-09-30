package com.household.household.enums;

public enum CarCompany {

    TATA_MOTORS,
    MARUTI_SUZUKI,
    HYUNDAI,
    TATA,
    MAHINDRA,
    KIA,
    TOYOTA,
    HONDA,
    RENAULT,
    SKODA,
    VOLKSWAGEN,
    MG,
    NISSAN,
    JEEP,
    FORD,
    CHEVROLET,
    FIAT,
    DATSUN,
    OTHER;

    public static CarCompany fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String normalized = text.trim().toUpperCase().replace(" ", "_").replace("-", "_");
        for (CarCompany c : CarCompany.values()) {
            if (c.name().equals(normalized)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Unknown CarCompany: " + text);
    }
}
