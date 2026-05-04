package com.app.assetmonitoringsystem.exception;

/**
 * Exception thrown for invalid business operations.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
