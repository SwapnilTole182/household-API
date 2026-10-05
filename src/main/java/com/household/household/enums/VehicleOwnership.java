package com.household.household.enums;

public enum VehicleOwnership {

    FIRST(1),
    SECOND(2),
    THIRD(3),
    FOURTH(4),
    FIFTH(5);

    private final int ownerNumber;

    VehicleOwnership(int ownerNumber) {
        this.ownerNumber = ownerNumber;
    }

    public int getOwnerNumber() {
        return ownerNumber;
    }
}
