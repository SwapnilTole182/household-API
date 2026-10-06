package com.household.household.enums;

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
    MERCEDES_BENZ_INDIA,
    MASERATI_INDIA,
    MAHINDRA_INDIA,
    LEXUS_INDIA,
    LAMBORGHINI_INDIA,
    KIA_INDIA,
    JEEP_INDIA,
    JAGUAR_LAND_ROVER_INDIA,
    ISUZU_MOTORS_INDIA,
    HONDA_CARS_INDIA,
//    GENERAL_MOTORS_INDIA,
    CHEVROLET_INDIA,
    FORD_INDIA,
    FORCE_MOTORS_CARS,
    FIAT_INDIA_AUTOMOBILES,
    FERRARI_INDIA,
    DATSUN_INDIA,
    CITROEN_INDIA,
    BYD_INDIA,
    BMW_INDIA,
    BENTLEY_MOTORS_INDIA,
    AUDI_INDIA,
    ASTON_MARTIN_INDIA,

    MG_MOTOR_INDIA,
    MINI_INDIA,
    MITSUBISHI_MOTORS_INDIA,
    NISSAN_MOTOR_INDIA,
    PORSCHE_INDIA,
    RENAULT_INDIA,
    ROLLS_ROYCE_MOTOR_CARS_INDIA,
    SKODA_AUTO_INDIA,
    TESLA_INDIA,
    TOYOTA_KIRLOSKAR_MOTOR,
    VINFAST_INDIA,
    VOLKSWAGEN_INDIA,
    VOLVO_CARS_INDIA,

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
