package com.household.household.enums;

import java.text.Normalizer;

public enum CarCompany {

    TATA_MOTORS,
    MARUTI_SUZUKI,
    HYUNDAI,
    TATA,
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
    MERCEDES_BENZ,
    MASERATI,
    MAHINDRA,
    LEXUS,
    LAMBORGHINI,

    JAGUAR_LAND_ROVER,
    LAND_ROVER,
    ISUZU,
    HONDA_CARS,
    FORCE_MOTORS,
    FIAT_INDIA_AUTOMOBILES,
    FERRARI,
    CITROEN,
    BYD_MOTORS,
    BMW,
    BENTLEY,
    BENTLEY_MOTORS,
    AUDI,
    ASTON_MARTIN,

    MG_MOTOR,
    MINI,
    MITSUBISHI_MOTORS,
    NISSAN_MOTOR,
    PORSCHE,
    ROLLS_ROYCE,
    ROLLS_ROYCE_MOTOR_CARS,
    SKODA_AUTO,
    TESLA,
    TOYOTA_KIRLOSKAR_MOTOR,
    VINFAST,
    VOLVO,
    VOLVO_CARS,
    VINFAST_MOTORS,

    OTHER;

    public static CarCompany fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String stripped = Normalizer.normalize(text.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String normalized = stripped.toUpperCase().replace(" ", "_").replace("-", "_");
        for (CarCompany c : CarCompany.values()) {
            if (c.name().equals(normalized)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Unknown CarCompany: " + text);
    }
}
