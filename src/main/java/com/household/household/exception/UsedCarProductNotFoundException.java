package com.household.household.exception;

public class UsedCarProductNotFoundException extends RuntimeException {

    public UsedCarProductNotFoundException(String message) {
        super(message);
    }

    public UsedCarProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
