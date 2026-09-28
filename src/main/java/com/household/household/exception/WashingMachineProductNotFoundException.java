package com.household.household.exception;

public class WashingMachineProductNotFoundException extends RuntimeException {

    public WashingMachineProductNotFoundException(String message) {
        super(message);
    }

    public WashingMachineProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
