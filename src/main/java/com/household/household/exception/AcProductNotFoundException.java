package com.household.household.exception;

public class AcProductNotFoundException extends RuntimeException {

    public AcProductNotFoundException(String message) {
        super(message);
    }

    public AcProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
